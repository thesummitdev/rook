package dev.thesummit.rook.models;

import dev.thesummit.rook.database.DatabaseField;
import dev.thesummit.rook.database.DatabaseListField;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.apache.commons.validator.routines.UrlValidator;
import org.json.JSONObject;

public class Link implements PageableBaseModel {
  private static final String[] URL_SCHEMES = {"http", "https"};
  private static final UrlValidator urlValidator = new UrlValidator(URL_SCHEMES);

  @DatabaseField(isId = true, orderBy = true) public Integer id;

  @DatabaseField() public String url;

  @DatabaseField() @DatabaseListField(seperator = " ") public String tags;

  @DatabaseField(whereOperator = " LIKE ", valueWrapper = "%") public String title;

  @DatabaseField() public Integer userId;

  @DatabaseField(isSetByDatabase = true) public Timestamp modified;

  public Link(String title, String url, String tags, int userId) {
    this.url = url;
    this.tags = tags;
    this.userId = userId;
    this.title = title;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return this.id;
  }

  public static Link fromJSONObject(JSONObject obj) {
    Link l = new Link(obj.getString("title"), obj.getString("url"),
        obj.optString("tags", "").toLowerCase(), obj.getInt("userId"));

    if (obj.has("id")) {
      l.setId(obj.getInt("id"));
    }

    return l;
  }

  /**
   * Make a JSON compliant object from the link instance.
   *
   * @return The JSONObject.
   */
  public JSONObject toJSONObject() {
    JSONObject obj = new JSONObject()
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
      Link l = new Link(
          rs.getString("title"), rs.getString("url"), rs.getString("tags"), rs.getInt("userId"));
      l.setId(rs.getInt("id"));
      l.modified = Timestamp.valueOf(rs.getString("modified"));
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

  public Integer getCursor() {
    return id;
  }
}
