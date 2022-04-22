package dev.thesummit.rook.models;

import dev.thesummit.rook.database.DatabaseField;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.JSONObject;

public class Preference implements BaseModel {

  public static Set<String> applicationPrefs =
      Stream.of("appVersion", "allowNewUsers").collect(Collectors.toCollection(HashSet::new));

  @DatabaseField(isId = true)
  public UUID id;

  @DatabaseField() public UUID userId;

  @DatabaseField(isIdentifier = true)
  public String key;

  @DatabaseField() public String value;

  public Preference(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public static Preference fromResultSet(ResultSet rs) {
    try {
      Preference p = new Preference(rs.getString("key"), rs.getString("value"));
      p.setId(rs.getObject("id", UUID.class));
      p.userId = (rs.getObject("userId", UUID.class));
      return p;
    } catch (SQLException e) {
      return null;
    }
  }

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
    JSONObject obj =
        new JSONObject().put("id", this.id).put("key", this.key).put("value", this.value);
    return obj;
  }

  @Override
  public Boolean isValid() {
    if (applicationPrefs.contains(this.key)) {
      // If it's an app pref, ensure it's not tied to a user.
      return this.key != null && this.value != null && this.userId == null;
    } else {
      // If it's not an app pref, ensure it's tied to a user.
      return this.key != null && this.value != null && this.userId != null;
    }
  }
}
