package dev.thesummit.rook.models;

import dev.thesummit.rook.database.DatabaseField;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONObject;

public class ApiKey implements BaseModel {

  @DatabaseField(isId = true)
  public Integer id;

  @DatabaseField public Integer userId;

  @DatabaseField public String key;

  public ApiKey(User user, String apiKey) {
    this.userId = user.getId();
    this.key = apiKey;
  }

  public ApiKey(Integer userId, String apiKey) {
    this.userId = userId;
    this.key = apiKey;
  }

  public static ApiKey fromResultSet(ResultSet rs) {
    try {
      return new ApiKey(rs.getInt("userId"), rs.getString("key"));
    } catch (SQLException e) {
      return null;
    }
  }

  @Override
  public Boolean isValid() {
    return this.userId != null && this.key != null;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public Integer getId() {
    return this.id;
  }

  @Override
  public JSONObject toJSONObject() {
    return new JSONObject().put("userId", userId).put("apiKey", key);
  }
}
