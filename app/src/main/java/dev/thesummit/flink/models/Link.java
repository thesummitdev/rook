package dev.thesummit.flink.models;

import dev.thesummit.flink.FlinkApplication;
import dev.thesummit.flink.database.DatabaseObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.apache.commons.validator.routines.UrlValidator;
import org.json.JSONObject;

public class Link implements DatabaseObject {

  private UUID id;
  private String url;
  private String tags;
  private Boolean unread;

  private static final String[] URL_SCHEMES = {"http", "https"};
  private static final UrlValidator urlValidator = new UrlValidator(URL_SCHEMES);
  private static final String INSERT_QUERY =
      "INSERT INTO links (url, tags, unread) VALUES (?,?,?) RETURNING id;";
  private static final String DELETE_QUERY = "DELETE FROM links WHERE id=?;";
  private static final String UPDATE_QUERY = "UPDATE links SET url=?, tags=?, unread=? WHERE id=? ";

  /**
   * Construct a Link instance.
   *
   * <p><b>Warning: Does not save the instance to the database. call .add() to commit to database.
   * </b>
   *
   * <p>The id property will remain unset until the instance is added to the database.
   *
   * @param url the url.
   * @param tags the tags.
   * @param unread whether the link is marked as unread.
   */
  public Link(String url, String tags, Boolean unread) {
    this.url = url;
    this.tags = tags;
    this.unread = unread;
  }

  /**
   * Get a link from the database.
   *
   * <p><b>WARNING:</b> Will use a connection from the application pool.
   *
   * @param uuid The link's id.
   * @return The Link or NULL if not found.
   */
  public static Link get(String uuid) {

    Connection conn = FlinkApplication.getContext().pool.getConnection();
    try {
      return Link.get(uuid, conn);
    } finally {
      FlinkApplication.getContext().pool.releaseConnection(conn);
    }
  }

  /**
   * Get a link from the database.
   *
   * @param uuid The link's id.
   * @param conn The database connection to use.
   * @return The Link or NULL if not found.
   */
  public static Link get(String uuid, Connection conn) {

    // TODO
    // https://stackoverflow.com/questions/46433459/postgres-select-where-the-where-is-uuid-or-string
    try (PreparedStatement statement =
        conn.prepareStatement("SELECT * FROM LINKS where id::text = ?;")) {
      statement.setString(1, uuid);
      try (ResultSet rs = statement.executeQuery()) {
        if (!rs.next()) {
          return null;
        }
        return Link.fromResultSet(rs);
      }
    } catch (SQLException e) {
      return null;
    }
  }

  public static List<Link> getAll(List<String> uidds) throws SQLException {
    return null;
  }

  /**
   * Adds this link instance to the database.
   *
   * <p>Fetches a connection from the application connection pool and attempts to add this link to
   * the database. Will append the database generated id to this instance when successful.
   *
   * <p><b>WARNING:</b> Will use a connection from the application pool.
   *
   * @throws SQLException
   */
  @Override
  public void put() throws SQLException {
    Connection conn = FlinkApplication.getContext().pool.getConnection();

    try {
      this.put(conn);
    } finally {

      FlinkApplication.getContext().pool.releaseConnection(conn);
    }
  }

  /**
   * Adds this link instance to the database.
   *
   * <p>Fetches a connection from the application connection pool and attempts to add this link to
   * the database. Will append the database generated id to this instance when successful.
   *
   * @param conn The database connection to use.
   * @throws SQLException
   */
  @Override
  public void put(Connection conn) throws SQLException {

    try (PreparedStatement statement = conn.prepareStatement(Link.INSERT_QUERY)) {

      statement.setString(1, this.url);
      statement.setString(2, this.tags);
      statement.setBoolean(3, this.unread);

      try (ResultSet rs = statement.executeQuery()) {

        rs.next(); // Position the RS for the returned ID.
        this.setId(rs.getObject("id", UUID.class));
      }
    }
  }

  /**
   * Removes this link from the database.
   *
   * <p>Fetches a connection from the application connection pool and attempts to remove this link
   * from the database.
   *
   * <p><b>WARNING:</b> Will use a connection from the application pool.
   *
   * @param id The id of the link to delete.
   * @throws SQLException
   */
  public static void delete(String id) throws SQLException {
    Connection conn = FlinkApplication.getContext().pool.getConnection();

    try {
      Link.delete(id, conn);
    } finally {
      FlinkApplication.getContext().pool.releaseConnection(conn);
    }
  }

  /**
   * Removes this link from the database.
   *
   * <p>Fetches a connection from the application connection pool and attempts to remove this link
   * from the database.
   *
   * @param id The id of the link to delete.
   * @param conn The database connection to use.
   * @throws SQLException
   */
  public static void delete(String id, Connection conn) throws SQLException {

    try (PreparedStatement statement = conn.prepareStatement(Link.DELETE_QUERY)) {
      statement.setString(1, id);
      statement.execute();
    }
  }

  /**
   * Updates the corresponding link row in the database to match this instance.
   *
   * <p>Fetches a connection from the application connection pool and attempts to update the current
   * link row in database.
   *
   * <p><b>WARNING:</b> Will use a connection from the application pool.
   *
   * @throws SQLException
   */
  @Override
  public void patch() throws SQLException {

    Connection conn = FlinkApplication.getContext().pool.getConnection();
    try {
      this.patch(conn);
    } finally {
      FlinkApplication.getContext().pool.releaseConnection(conn);
    }
  }

  /**
   * Updates the corresponding link row in the database to match this instance.
   *
   * <p>Fetches a connection from the application connection pool and attempts to update the current
   * link row in database.
   *
   * @param conn The database connection to use.
   * @throws SQLException
   */
  @Override
  public void patch(Connection conn) throws SQLException {

    try (PreparedStatement statement = conn.prepareStatement(Link.UPDATE_QUERY)) {
      statement.setString(1, this.url);
      statement.setString(2, this.tags);
      statement.setBoolean(3, this.unread);
      statement.setObject(4, this.id);
      statement.execute();
    }
  }

  /**
   * Make a human-readable string representaiton of this Link object.
   *
   * <p>${id} / ${url} / {$tags}
   *
   * @return A printable, displayable, transmittable representation of the Link.
   */
  @Override
  public String toString() {
    return String.format(
        "Link id %s, with a url of %s and tags of %s", this.id, this.url, this.tags);
  }

  /**
   * Make a JSON compliant object from the link instance.
   *
   * @return The JSONObject.
   */
  public JSONObject toJSONObject() {
    JSONObject obj =
        new JSONObject()
            .put("id", this.id)
            .put("url", this.url)
            .put("tags", this.tags)
            .put("unread", this.unread);

    return obj;
  }

  /**
   * Make a JSON compliant object from the link instance.
   *
   * @return The JSONObject.
   */
  public static Link fromJSONObject(JSONObject obj) {

    Link l = new Link(obj.getString("url"), obj.getString("tags"), obj.getBoolean("unread"));

    if (obj.has("id")) {
      l.setId((UUID) obj.get("id"));
    }

    return l;
  }

  /**
   * Create a Link instance from a SQL ResultSet row.
   *
   * <p>Does not call rs.next() before or after, operates on the current row.
   *
   * @param rs A result set row.
   * @return The link.
   */
  public static Link fromResultSet(ResultSet rs) throws SQLException {
    Link l = new Link(rs.getString("url"), rs.getString("tags"), rs.getBoolean("unread"));
    l.setId(rs.getObject("id", UUID.class));
    return l;
  }

  /**
   * Ensures the Link contains valid data and can be safely committed to the database.
   *
   * @return Whether the Link is Valid or not.
   */
  public Boolean isValid() {
    return urlValidator.isValid(this.url);
  }

  /**
   * Getter for the link.id field.
   *
   * @return The id value.
   */
  public UUID getId() {
    return id;
  }

  /**
   * Internal Setter for the link.id field.
   *
   * <p><b> Warning: Must be a UUID </b>
   *
   * @param id The id to set.
   */
  private void setId(UUID id) {
    this.id = id;
  }

  /**
   * Getter for the link.url field.
   *
   * @return The url value.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Setter for the link.url field.
   *
   * <p><b> Warning: Must be a valid URL </b>
   *
   * @param url The url to set.
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Getter for the link.tags field.
   *
   * @return The tags value.
   */
  public String getTags() {
    return tags;
  }

  /**
   * Setter for the link.tags field.
   *
   * @param tags The tags to set.
   */
  public void setTags(String tags) {
    this.tags = tags;
  }

  /**
   * Getter for the link.unread field.
   *
   * @return The unread value.
   */
  public Boolean getUnread() {
    return unread;
  }

  /**
   * Setter for the link.unread field.
   *
   * @param unread The unread value to set.
   */
  public void setUnread(Boolean unread) {
    this.unread = unread;
  }
}
