package dev.thesummit.flink.models;

import dev.thesummit.flink.database.DatabaseField;
import java.util.UUID;
import org.json.JSONObject;

public class Preference implements BaseModel {

  @DatabaseField(isId = true)
  public UUID id;

  @DatabaseField() public String key;
  @DatabaseField() public String value;

  public Preference(String key, String value) {
    this.key = key;
    this.value = value;
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
    return this.key != null && this.value != null;
  }
}
