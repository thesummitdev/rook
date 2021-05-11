package dev.thesummit.flink.models;

import dev.thesummit.flink.FlinkApplication;
import dev.thesummit.flink.database.DatabaseObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class Link implements DatabaseObject {

  private UUID id;
  private String url;
  private String tags;
  private Boolean unread;

  private static String INSERT_QUERY =
      "INSERT INTO links (url, tags, unread) VALUES (?,?,?) RETURNING id;";
  private static String DELETE_QUERY = "DELETE FROM links WHERE id=?;";
  private static String UPDATE_QUERY = "UPDATE links SET url=?, tags=?, unread=? WHERE id=? ";

  public Link(String url, String tags, Boolean unread) {
    this.url = url;
    this.tags = tags;
    this.unread = unread;
  }

  public static Link get(String uidd) throws SQLException {
    return null;
  }

  public static List<Link> getAll(List<String> uidds) throws SQLException {
    return null;
  }

  public UUID getId() {
    return id;
  }

  private void setId(UUID id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTags() {
    return tags;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  public Boolean getUnread() {
    return unread;
  }

  public void setUnread(Boolean unread) {
    this.unread = unread;
  }

  /** String representation */
  @Override
  public String toString() {
    return String.format(
        "Link id %s, with a url of %s and tags of %s", this.id, this.url, this.tags);
  }

  @Override
  public void add() throws SQLException {
    Connection conn = FlinkApplication.getContext().pool.getConnection();

    PreparedStatement statement = conn.prepareStatement(Link.INSERT_QUERY);
    statement.setString(1, this.url);
    statement.setString(2, this.tags);
    statement.setBoolean(3, this.unread);

    ResultSet rs = statement.executeQuery();
    rs.next(); // Position the RS for the returned ID.
    this.setId(rs.getObject("id", UUID.class));

    statement.close();
    FlinkApplication.getContext().pool.releaseConnection(conn);
  }

  public static void delete(String id) throws SQLException {
    Connection conn = FlinkApplication.getContext().pool.getConnection();

    PreparedStatement statement = conn.prepareStatement(Link.DELETE_QUERY);
    statement.setString(1, id);
    statement.execute();

    statement.close();
    FlinkApplication.getContext().pool.releaseConnection(conn);
  }

  @Override
  public void update() throws SQLException {
    Connection conn = FlinkApplication.getContext().pool.getConnection();

    PreparedStatement statement = conn.prepareStatement(Link.UPDATE_QUERY);
    statement.setString(1, this.url);
    statement.setString(2, this.tags);
    statement.setBoolean(3, this.unread);
    statement.setObject(4, this.id);
    statement.execute();

    statement.close();
    FlinkApplication.getContext().pool.releaseConnection(conn);
  }
}
