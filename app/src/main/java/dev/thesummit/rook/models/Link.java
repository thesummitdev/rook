package dev.thesummit.rook.models;

import dev.thesummit.rook.database.DatabaseArrayField;
import dev.thesummit.rook.database.DatabaseField;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;
import org.apache.commons.validator.routines.UrlValidator;
import org.json.JSONObject;

public class Link implements BaseModel {

  private static final String[] URL_SCHEMES = {"http", "https"};
  private static final UrlValidator urlValidator = new UrlValidator(URL_SCHEMES);

  @DatabaseField(isId = true)
  public UUID id;

  @DatabaseField() public String url;

  @DatabaseField()
  @DatabaseArrayField(
      arrayFuncton = "string_to_array(tags, ' ')",
      arrayCompareOperator = " @> ",
      arraySeperator = " ")
  public String tags;

  @DatabaseField(whereOperator = " ~* ")
  public String title;

  @DatabaseField() public UUID userId;

  @DatabaseField(isSetByDatabase = true)
  public Timestamp modified;

  public Link(String title, String url, String tags, UUID userId) {
    this.url = url;
    this.tags = tags;
    this.userId = userId;
    this.title = title;
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
            obj.getString("title"),
            obj.getString("url"),
            obj.optString("tags", ""),
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
            .put("title", this.title)
            .put("url", this.url)
            .put("tags", this.tags);

    if (this.modified != null) {
      obj.put("modified", this.modified.getTime());
    }

    return obj;
  }

  public static Link fromResultSet(ResultSet rs) {
    try {
      Link l =
          new Link(
              rs.getString("title"),
              rs.getString("url"),
              rs.getString("tags"),
              rs.getObject("userId", UUID.class));
      l.setId(rs.getObject("id", UUID.class));
      l.modified = rs.getTimestamp("modified");
      System.out.println(l.modified.toString());
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
