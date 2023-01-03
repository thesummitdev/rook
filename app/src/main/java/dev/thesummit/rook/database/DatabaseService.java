package dev.thesummit.rook.database;

import dev.thesummit.rook.models.BaseModel;
import dev.thesummit.rook.models.PageableBaseModel;
import java.util.List;
import java.util.Map;

/**
 * The contract between the API handlers and the database.
 * See {@link RookDatabaseService} for an implementation.
 */
public interface DatabaseService {
  /**
   * Attempt to add the entity to the database.
   *
   * <p>This has the side effect of calling {@link BaseModel#setId(Integer)} on the entity. (If the
   * entity has an ID set after this call completes, it was successfully added.)
   *
   * @param entity if successful, this entity will be added to the database.
   */
  public void put(BaseModel entity);

  /**
   * Update an existing entity in the database.
   *
   * <p>The database is updated to match the recognized fields of the passed in entity. If the
   * entity does not exist in the database this does nothing.
   *
   * @param entity if successful, this entity's database row will be updated to match.
   */
  public void patch(BaseModel entity);

  /**
   * Attempts to remove the entity from the database.
   *
   * <p>If successful, the entity will no longer exist in the database.
   *
   * @param entity the entity to remove from the database.
   *
   */
  public void delete(BaseModel entity);

  /**
   * Attempts to fetch the provided entity type by ID lookup.
   *
   * <p>This is for lookups where the table has a known id column / primary key.
   *
   * @param  <T> A class that extends {@link BaseModel}.
   * @param  cls The base class of the entity to search for.
   * @param  id  The specific row id to fetch.
   * @return     Attempts to find the row for the given entity class. Null if not found.
   */
  public <T extends BaseModel> T get(Class<T> cls, Integer id);

  /**
   * Attempts to fetch the provided entity type by identifier lookup.
   *
   * <p>This is for lookups where the table does not have a known id column / primary key.
   * (The class can declare a Identifier with the {@link DatabaseField} decorator.)
   *
   * @param  <T>         A class that extends {@link BaseModel}.
   * @param  cls         The base class of the entity to search for.
   * @param  identifier  The identifier to locate the row to fetch.
   * @return             Attempts to find the row for the given entity class. Null if not found.
   */
  public <T extends BaseModel> T get(Class<T> cls, String identifier);

  /**
   * Attempts to fetch all entities that match the provided parameters.
   *
   * <p>The provided paramater keys and types must match the declared fields of the entity class.
   *
   * @param  <T>    A class that extends {@link BaseModel}.
   * @param  cls    The base class of the entity to search for.
   * @param  params the map of param keys to values to filter the query with.
   * @return        Attempts to find rows for the given entity class that match the search
   *                parameters. Empty list if none are found.
   */
  public <T extends BaseModel> List<T> getAll(Class<T> cls, Map<String, Object> params)
      throws IllegalArgumentException;

  /**
   * Attempts to fetch paged results of entities that match the provided parameters.
   *
   * <p>The provided paramater keys and types must match the declared fields of the entity class.
   * <strong>Note:</strong> the params may contain additional keys: <code>limit, cursor</code>
   *
   * @param  <T>    A class that extends {@link PageableBaseModel}.
   * @param  cls    The base class of the entity to search for.
   * @param  params the map of param keys to values to filter the query with.
   * @return        Attempts to find rows for the given entity class that match the search
   *                parameters. Empty list if none are found.
   *
   * @see           PagedResults
   * @see           PagedCursor
   */
  public <T extends PageableBaseModel> PagedResults<T> getAllPaged(
      Class<T> cls, Map<String, Object> params) throws IllegalArgumentException;
}
