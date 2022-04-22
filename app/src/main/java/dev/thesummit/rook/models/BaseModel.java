package dev.thesummit.rook.models;

import java.sql.ResultSet;
import java.util.UUID;
import org.json.JSONObject;

public interface BaseModel {

  public void setId(UUID id);

  public UUID getId();

  public static BaseModel fromJSONObject(JSONObject obj) {
    return null;
  }

  public static BaseModel fromResultSet(ResultSet rs) {
    return null;
  }

  public JSONObject toJSONObject();

  public Boolean isValid();
}
