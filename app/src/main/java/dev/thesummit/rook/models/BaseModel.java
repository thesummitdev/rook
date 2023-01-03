package dev.thesummit.rook.models;

import java.sql.ResultSet;
import org.json.JSONObject;

/**
 * The base database object. All API accessible entities must implement this interface.
 */
public interface BaseModel {
  /**
   * Sets the ID on this entity.
   *
   * <p>In general, you should not need to call this. This method exists for the database hooks
   * to automatically set the entity id for new rows inserted into the database.
   */
  public void setId(Integer id);

  /**
   * Gets the corresponding database row id for this entity.
   *
   * @return The corresponding database row id for this entity.
   */
  public Integer getId();

  /**
   * Converts the entity from a {@link JSONObject} to it's {@link BaseModel} implementation.
   *
   * <p>Note: This doesn't mean all java fields are converted. Depends on the specific entity's
   * implementation.
   *
   * @return the {@link BaseModel} representation of this entity.
   */
  public static BaseModel fromJsonObject(JSONObject obj) {
    return null;
  }

  /**
   * Converts the entity from a {@link ResultSet} to it's {@link BaseModel} implementation.
   *
   * <p>Note: This doesn't mean all java fields are converted. Depends on the specific entity's
   * implementation.
   *
   * @return the {@link BaseModel} representation of this entity.
   */
  public static BaseModel fromResultSet(ResultSet rs) {
    return null;
  }

  /**
   * Converts the entity into a {@link JSONObject}.
   *
   * <p>Note: This doesn't mean all java fields are converted. Depends on the specific entity's
   * implementation.
   *
   * @return the json representation of this entity.
   *
   */
  public JSONObject toJsonObject();

  /**
   * Checks if the entity is currently valid and could be added/updated in the database.
   *
   * @return if the entity is currently valid for database operations.
   */
  public Boolean isValid();
}
