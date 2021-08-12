package dev.thesummit.flink.models;

import dev.thesummit.flink.database.DatabaseField;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.json.JSONObject;

public class Link implements BaseModel {

  @DatabaseField(isId = true)
  public UUID id;

  @DatabaseField() public String url;

  @DatabaseField() public String tags;

  @DatabaseField() public Boolean unread;

  public Link(String url, String tags, Boolean unread) {
    this.url = url;
    this.tags = tags;
    this.unread = unread;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getId() {
    return this.id;
  }

  public static Link fromJSONObject(JSONObject obj) {
    Link l = new Link(obj.getString("url"), obj.getString("tags"), obj.getBoolean("unread"));

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
      Link l = new Link(rs.getString("url"), rs.getString("tags"), rs.getBoolean("unread"));
      l.setId(rs.getObject("id", UUID.class));
      return l;
    } catch (SQLException e) {
      return null;
    }
  }

  public Boolean isValid() {
    return true;
  }
}
