package dev.thesummit.rook.models;

import dev.thesummit.rook.database.DatabaseField;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.json.JSONObject;

public class SystemKey implements BaseModel {

  public SystemKey(String key, String value) {
    this.key = key;
    this.value = value;
  }

  @DatabaseField(isId = true)
  public UUID id;

  @DatabaseField(isIdentifier = true)
  public String key;

  @DatabaseField public String value;

  @Override
  public void setId(UUID id) {
    this.id = id;
  }

  @Override
  public UUID getId() {
    return this.id;
  }

  @Override
  public JSONObject toJSONObject() {
    JSONObject obj = new JSONObject().put("key", this.key).put("value", this.value);
    return obj;
  }

  @Override
  public Boolean isValid() {
    return this.key != null && this.value != null;
  }

  public static SystemKey fromResultSet(ResultSet rs) {
    try {
      SystemKey sk = new SystemKey(rs.getString("key"), rs.getString("value"));
      try {
        // Just in case ID doesn't exist in the schema yet.
        sk.setId(rs.getObject("id", UUID.class));
      } catch (SQLException e) {
        e.printStackTrace();
      }
      return sk;
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }
}
