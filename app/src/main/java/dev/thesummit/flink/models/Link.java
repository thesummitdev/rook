package dev.thesummit.flink.models;

import dev.thesummit.flink.database.DatabaseField;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.commons.validator.routines.UrlValidator;
import org.json.JSONObject;

public class Link implements BaseModel {

  private static final String[] URL_SCHEMES = {"http", "https"};
  private static final UrlValidator urlValidator = new UrlValidator(URL_SCHEMES);

  @DatabaseField(isId = true)
  public UUID id;

  @DatabaseField() public String url;

  @DatabaseField(whereOperator = " ~ ")
  public String tags;

  @DatabaseField() public Boolean unread;

  @DatabaseField() public UUID userId;

  public Link(String url, String tags, Boolean unread, UUID userId) {
    this.url = url;
    this.tags = tags;
    this.unread = unread;
    this.userId = userId;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getId() {
    return this.id;
  }

  public static Link fromJSONObject(JSONObject obj) {
    Link l =
        new Link(
            obj.getString("url"),
            obj.optString("tags", ""),
            obj.optBoolean("unread", false),
            UUID.fromString(obj.getString("userId")));

    if (obj.has("id")) {
      l.setId((UUID) obj.get("id"));
    }

    return l;
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

  public static Link fromResultSet(ResultSet rs) {
    try {
      Link l =
          new Link(
              rs.getString("url"),
              rs.getString("tags"),
              rs.getBoolean("unread"),
              rs.getObject("userId", UUID.class));
      l.setId(rs.getObject("id", UUID.class));
      return l;
    } catch (SQLException e) {
      return null;
    }
  }

  /**
   * Ensure the url is valid.
   *
   * @return whether the link object is valid.
   */
  public Boolean isValid() {
    return urlValidator.isValid(this.url);
  }
}
