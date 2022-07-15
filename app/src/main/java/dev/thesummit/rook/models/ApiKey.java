package dev.thesummit.rook.models;

import dev.thesummit.rook.database.DatabaseField;
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
