package dev.thesummit.rook.database;

import com.google.inject.Inject;
import dev.thesummit.rook.models.BaseModel;
import dev.thesummit.rook.models.PageableBaseModel;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation for the {@link DatabaseService}.
 */
public class RookDatabaseService implements DatabaseService {
  private static Logger log = LoggerFactory.getLogger(RookDatabaseService.class);
  private ConnectionPool pool;
  private HashMap<Class<?>, String> tableMapping;
  private Object lock = new Object();

  @Inject
  public RookDatabaseService(ConnectionPool pool, HashMap<Class<?>, String> tableMapping) {
    this.pool = pool;
    this.tableMapping = tableMapping;
  }

  @Override
  public void put(BaseModel entity) {
    String tableName = this.tableMapping.get(entity.getClass());

    String fields = this.getQueryFieldsString(
        entity.getClass(), /* includeId= */ true, /* includeFieldsSetByDatabase= */ false);
    ArrayList<Object> values = this.getQueryFieldValues(entity, /* includeId= */ true, false);
    String valuePlaceholders = "?";
    if (values.size() > 1) {
      valuePlaceholders = "?,".repeat(values.size() - 1) + "?";
    }

    StringBuilder query = new StringBuilder(String.format(
        "INSERT into %s (%s) VALUES (%s) returning *;", tableName, fields, valuePlaceholders));
    log.info(query.toString());
    log.info(values.toString());

    Connection conn = this.pool.getConnection();
    try (PreparedStatement statement = conn.prepareStatement(query.toString())) {
      int fieldIndex = 1;
      for (Object v : values) {
        if (v instanceof Integer) {
          statement.setInt(fieldIndex, (int) v);
        } else if (v instanceof Boolean) {
          statement.setBoolean(fieldIndex, (boolean) v);

        } else if (v instanceof String) {
          statement.setString(fieldIndex, v.toString());
        } else {
          statement.setObject(fieldIndex, v);
        }
        fieldIndex++;
      }
      log.debug(String.format("PUT SQL: %s", statement.toString()));

      synchronized (lock) {
        try (ResultSet rs = statement.executeQuery()) {
          while (rs.next()) {
            entity.setId(rs.getInt("id"));

            for (Field f : entity.getClass().getDeclaredFields()) {
              DatabaseField annotation = f.getAnnotation(DatabaseField.class);
              if (annotation == null) {
                continue;
              }
              if (annotation.isSetByDatabase()) {
                try {
                  if (f.getType() == Timestamp.class) {
                    f.set(entity, Timestamp.valueOf(rs.getString(f.getName())));
                  } else {
                    f.set(entity, rs.getObject(f.getName()));
                  }
                } catch (IllegalAccessException e) {
                  log.debug(
                      "Field was not accessible, check fields marked with @DatabaseField are not"
                          + " private.",
                      e);
                }
              }
            }
          }
        }
      }
    } catch (SQLException e) {
      log.info("Unable to create entity, SQL error occured.", e);
    } finally {
      this.pool.releaseConnection(conn);
    }
  }

  @Override
  public void patch(BaseModel entity) {
    String tableName = this.tableMapping.get(entity.getClass());
    StringJoiner fields = new StringJoiner(",");

    Integer id = entity.getId();
    for (Field f : entity.getClass().getDeclaredFields()) {
      DatabaseField annotation = f.getAnnotation(DatabaseField.class);
      if (annotation == null || annotation.isSetByDatabase()) {
        continue;
      }
      if (annotation.isId()) {
        try {
          id = (Integer) f.get(entity);
        } catch (IllegalAccessException e) {
          log.debug(
              "Field was not accessible, check fields marked with @DatbaseField are not private.",
              e);
          return;
        }
      } else {
        fields.add(f.getName() + "=?"); // Include the parameter =? in the statement for each field.
      }
    }

    if (id == null) {
      return;
    }

    StringBuilder query = new StringBuilder(String.format(
        "UPDATE %s set %s WHERE id=%s returning *;", tableName, fields.toString(), id));

    Connection conn = this.pool.getConnection();
    try (PreparedStatement statement = conn.prepareStatement(query.toString())) {
      int fieldIndex = 1;
      for (Field f : entity.getClass().getDeclaredFields()) {
        DatabaseField annotation = f.getAnnotation(DatabaseField.class);
        if (annotation == null || annotation.isId() || annotation.isSetByDatabase()) {
          continue;
        }
        if (f.get(entity) == null) {
          statement.setNull(fieldIndex, java.sql.Types.OTHER);
        } else if (f.getType() == Boolean.class) {
          statement.setBoolean(fieldIndex, (Boolean) f.get(entity));
        } else if (f.getType() == Integer.class) {
          statement.setInt(fieldIndex, (Integer) f.get(entity));
        } else {
          statement.setString(fieldIndex, (String) f.get(entity));
        }
        fieldIndex++;
      }

      log.info(statement.toString());

      synchronized (lock) {
        try (ResultSet rs = statement.executeQuery()) {
          while (rs.next()) {
            entity.setId(rs.getInt("id"));
          }
        }
      }
    } catch (IllegalAccessException e) {
      log.debug(
          "Field was not accessible, check fields marked with @DatabaseField are not private.", e);
      return;
    } catch (SQLException e) {
      log.info("Unable to patch entity, SQL error occured.", e);
    } finally {
      this.pool.releaseConnection(conn);
    }
  }

  @Override
  public void delete(BaseModel entity) {
    String tableName = this.tableMapping.get(entity.getClass());
    String query = String.format("DELETE FROM %s where id=?;", tableName);

    Connection conn = this.pool.getConnection();

    try (PreparedStatement statement = conn.prepareStatement(query)) {
      statement.setInt(1, entity.getId());
      log.info(statement.toString());
      statement.execute();
    } catch (SQLException e) {
      log.debug("Unable to delete entity, SQL error occured.", e);
      e.printStackTrace();
    } finally {
      this.pool.releaseConnection(conn);
    }
  }

  @Override
  public <T extends BaseModel> T get(Class<T> cls, String identifier) {
    String tableName = this.tableMapping.get(cls);
    Connection conn = this.pool.getConnection();

    String fields = this.getQueryFieldsString(
        cls, /* includeId= */ true, /* includeFieldsSetByDatabase= */ true);
    HashMap<String, Field> fieldMap = this.getQueryFieldsMap(cls, true);
    String identifierField = null;

    for (Entry<String, Field> entry : fieldMap.entrySet()) {
      String name = entry.getKey();
      Field field = entry.getValue();
      DatabaseField annotation = field.getAnnotation(DatabaseField.class);

      if (annotation.isIdentifier()) {
        identifierField = name;
      }
    }

    if (identifierField == null) {
      log.debug("No identifierField field found for GET request.");
      return null;
    }

    StringBuilder query = new StringBuilder(String.format(
        "SELECT %s FROM %s WHERE %s='%s';", fields, tableName, identifierField, identifier));

    try (PreparedStatement statement = conn.prepareStatement(query.toString())) {
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          try {
            Method m = cls.getMethod("fromResultSet", ResultSet.class);
            @SuppressWarnings("unchecked") // Cast the Object to T for it to be returned.
            T entity = (T) m.invoke(null, rs);
            return entity;
          } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // Couldn't call fromResultSet
            log.debug("Method fromResultSet doesn't exist on Entity.");
            return null;
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    } finally {
      this.pool.releaseConnection(conn);
    }
    return null;
  }

  @Override
  public <T extends BaseModel> T get(Class<T> cls, Integer id) {
    String tableName = this.tableMapping.get(cls);
    Connection conn = this.pool.getConnection();

    String fields = this.getQueryFieldsString(
        cls, /* includeId= */ true, /* includeFieldsSetByDatabase= */ true);

    StringBuilder query =
        new StringBuilder(String.format("SELECT %s FROM %s WHERE id='%s';", fields, tableName, id));

    try (PreparedStatement statement = conn.prepareStatement(query.toString())) {
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          try {
            Method m = cls.getMethod("fromResultSet", ResultSet.class);
            @SuppressWarnings("unchecked") // Cast the Object to T for it to be returned.
            T entity = (T) m.invoke(null, rs);
            return entity;
          } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // Couldn't call fromResultSet
            e.printStackTrace();
            return null;
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    } finally {
      this.pool.releaseConnection(conn);
    }
    return null;
  }

  @Override
  public <T extends BaseModel> List<T> getAll(Class<T> cls, Map<String, Object> params)
      throws IllegalArgumentException {
    String fieldsString = this.getQueryFieldsString(
        cls, /* includeId= */ true, /* includeFieldsSetByDatabase= */ true);
    String tableName = this.tableMapping.get(cls);
    StringBuilder query =
        new StringBuilder(String.format("SELECT %s FROM %s", fieldsString, tableName));
    ArrayList<T> results = new ArrayList<T>();

    if (!params.isEmpty()) {
      query.append("\n WHERE ");
    }

    int index = 1;
    HashMap<String, Field> fieldsMap = this.getQueryFieldsMap(cls, /* includeId= */ true);
    for (String field : params.keySet()) {
      Field dbField = fieldsMap.getOrDefault(field, null);
      if (dbField == null) {
        // params don't match a known db field, so continue with params.
        continue;
      }

      DatabaseField annotation = dbField.getAnnotation(DatabaseField.class);
      DatabaseListField listAnnotation = dbField.getAnnotation(DatabaseListField.class);

      // Handle list fields differently.
      if (listAnnotation != null) {
        // Default implementation assumes this is a string list.

        if (params.get(field).equals("")) {
          continue;
        }

        if (index <= params.size() && index != 1) {
          query.append("\n AND ");
        }

        // get all list values we are querying for
        Object listValue = params.get(field);
        if (listValue instanceof String) {
          String[] values = ((String) params.get(field)).split(listAnnotation.seperator());
          int valueIndex = 1;
          for (String value : values) {
            query.append(field).append(" LIKE ").append(String.format("\"%%%s%%\"", value));
            if (valueIndex < values.length) {
              query.append("\n AND ");
            }
            valueIndex++;
          }
        } else {
          throw new IllegalArgumentException(String.format(
              "Query parameter %s was not a handleable field type for the declared field.", field));
        }

      } else {
        if (index <= params.size() && index != 1) {
          query.append("\n AND ");
        }

        if (params.get(field) == null) {
          // For fields where the value is actually null do an "IS NULL" compare.
          query.append(field).append(" is ").append("NULL");
        } else {
          query.append(field)
              .append(annotation.whereOperator())
              .append("?");
        }
      }
      if (index == params.size()) {
        query.append(";"); // End the Query
      }
      index++;
    }

    String sql = query.toString();
    log.info(sql);
    Connection conn = this.pool.getConnection();
    try (PreparedStatement statement = conn.prepareStatement(sql)) {
      index = 1;
      for (Map.Entry<String, Object> e : params.entrySet()) {
        Field dbField = fieldsMap.getOrDefault(e.getKey(), null);
        if (dbField == null) {
          // params don't match a known db field, so continue with params.
          continue;
        }
        DatabaseField annotation = dbField.getAnnotation(DatabaseField.class);
        DatabaseListField listAnnotation = dbField.getAnnotation(DatabaseListField.class);
        if (e.getValue() instanceof Boolean) {
          boolean b = (boolean) e.getValue();
          statement.setBoolean(index, b);
        } else if (e.getValue() instanceof Integer) {
          int i = (int) e.getValue();
          statement.setInt(index, i);
        } else if (e.getValue() == null) {
          // No parameter at this index, query was manually set to IS NULL.
          index++;
          continue;
        } else if (e.getValue() instanceof String) {
          if (listAnnotation != null) {
            // No parameter for this index, query values have already been set.
            index++;
            continue;
          } else {
            statement.setString(index,
                String.format("%s%s%s", annotation.valueWrapper(), e.getValue().toString(),
                    annotation.valueWrapper()));
          }

        } else {
          statement.setObject(index, e.getValue());
        }
        index++;
      }

      log.info("GetAll SQL: {}", statement.toString());
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          try {
            Method m = cls.getMethod("fromResultSet", ResultSet.class);
            @SuppressWarnings("unchecked") // Cast the Object to T for it to be returned.
            T entity = (T) m.invoke(null, rs);
            results.add(entity);
          } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // Couldn't call fromResultSet
            e.printStackTrace();
            return Collections.emptyList();
          }
        }
      }
    } catch (SQLException e) {
      log.info("Database error during getAll", e);
      return Collections.emptyList();
    } finally {
      this.pool.releaseConnection(conn);
    }

    return results;
  }

  @Override
  public <T extends PageableBaseModel> PagedResults<T> getAllPaged(
      Class<T> cls, Map<String, Object> params) throws IllegalArgumentException {
    PagedCursor requestCursor = null;

    // Ensure we were actually sent a string before casting it.
    if (params.get("cursor") instanceof String) {
      requestCursor = PagedCursor.parse((String) params.get("cursor"));
      if (requestCursor == null) {
        // A cursor was sent in the parameters, but it could not be parsed, so terminate here.
        throw new IllegalArgumentException("Invalid cursor was present in the request parameters.");
      }
    }

    String orderByField = null;
    for (Field f : cls.getDeclaredFields()) {
      DatabaseField annotation = f.getAnnotation(DatabaseField.class);
      if (annotation == null) {
        continue;
      }
      if (annotation.orderBy()) {
        orderByField = f.getName();
        break;
      }
    }

    if (orderByField == null) {
      throw new IllegalArgumentException("No orderable field found on model definition.");
    }

    HashMap<String, Field> fieldsMap = getQueryFieldsMap(cls, true);
    StringJoiner whereClauseJoiner =
        generateWhereClauseJoiner(fieldsMap, params, orderByField, requestCursor);

    String fieldsString = this.getQueryFieldsString(
        cls, /* includeId= */ true, /* includeFieldsSetByDatabase= */ true);
    String tableName = this.tableMapping.get(cls);

    int limit = 20;
    if (params.get("limit") instanceof Integer) {
      limit = (int) params.get("limit");
      if (limit > 100) {
        limit = 100;
      } else if (limit < 1) {
        limit = 1;
      }
    }

    // The query sort needs to be ASC if we're paging backwards so that we need to fetch them in
    // order from the cursor point. (The list is reversed before returning the request so they
    // appear in the same order.)
    String direction = requestCursor != null && requestCursor.hasPrev() ? "ASC" : "DESC";

    StringBuilder query = new StringBuilder(
        String.format("SELECT %s FROM %s WHERE %s ORDER BY %s %s LIMIT %s;", fieldsString,
            // use (limit + 1) so we can check existance of next page.
            tableName, whereClauseJoiner.toString(), orderByField, direction, limit + 1));
    ArrayList<T> results = new ArrayList<T>();

    String sql = query.toString();
    Connection conn = this.pool.getConnection();
    try (PreparedStatement statement = conn.prepareStatement(sql)) {
      int index = 1;
      for (Map.Entry<String, Object> e : params.entrySet()) {
        Field dbField = fieldsMap.getOrDefault(e.getKey(), null);

        if (e.getKey().equals("cursor")) {
          statement.setInt(index,
              requestCursor.hasNext() ? requestCursor.getNextCursor()
                  : requestCursor.hasPrev()
                  ? requestCursor.getPrevCursor()
                  // This null should result in a SQL error, but it's not possible to get here.
                  // (Since parsing the cursor should have failed above.)
                  : null);
          index++;
          continue;
        } else if (dbField == null) {
          // params don't match a known db field, so continue with params.
          continue;
        }

        DatabaseField annotation = dbField.getAnnotation(DatabaseField.class);
        DatabaseListField listAnnotation = dbField.getAnnotation(DatabaseListField.class);
        if (e.getValue() instanceof Boolean) {
          boolean b = (boolean) e.getValue();
          statement.setBoolean(index, b);
        } else if (e.getValue() instanceof Integer) {
          int i = (int) e.getValue();
          statement.setInt(index, i);
        } else if (e.getValue() == null) {
          // No parameter at this index, query was manually set to IS NULL.
          index++;
          continue;
        } else if (e.getValue() instanceof String) {
          if (listAnnotation != null) {
            // No parameter for this index, query values have already been set.
            index++;
            continue;
          } else {
            statement.setString(index,
                String.format("%s%s%s", annotation.valueWrapper(), e.getValue().toString(),
                    annotation.valueWrapper()));
          }

        } else {
          statement.setObject(index, e.getValue());
        }
        index++;
      }

      log.debug("GetAll SQL: {}", statement);
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          try {
            Method m = cls.getMethod("fromResultSet", ResultSet.class);
            @SuppressWarnings("unchecked") // Cast the Object to T for it to be returned.
            T entity = (T) m.invoke(null, rs);
            results.add(entity);
          } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // Couldn't call fromResultSet
            e.printStackTrace();
            return new PagedResults<T>(Collections.<T>emptyList(), requestCursor);
          }
        }
      }
    } catch (SQLException e) {
      log.error("Database error during getAllPaged", e);
      PagedResults<T> pr = new PagedResults<T>(Collections.<T>emptyList(), requestCursor);
      return pr;
    } finally {
      this.pool.releaseConnection(conn);
    }

    boolean hasNextPageInDirection = false;

    // Remove last element before returning results so we match the expected page size.
    if (results.size() > limit) {
      hasNextPageInDirection = true;
      results.remove(results.size() - 1);
    }

    // If this is a asc query, we need to reverse the results before returning, since we want
    // the higher ids on top.
    if (direction.equals("ASC")) {
      Collections.reverse(results);
    }

    // These start out as null because they are not always set.
    Integer prevCursor = null;
    Integer nextCursor = null;

    // Set the previous cursor if:
    // If the request cursor is present & the next page was requested
    // (which means that we have a page to go back to.)
    // ----
    // Or if the request cursor is requesting to go backwards, and there is still another page
    // available beyond the current page.
    if ((requestCursor != null && requestCursor.hasNext())
        || (hasNextPageInDirection && (direction.equals("ASC")))) {
      // Cursor should be the first element in the list.
      prevCursor = results.get(0).getCursor();
    }

    // Set the next cursor if:
    // If the request cursor is present and the previous page was requested
    // (which means that we have a page to go forward to.)
    // ----
    // Or if the request cursor is requesting to go forwards, and there is still another page
    // available beyond the current page.
    if ((requestCursor != null && requestCursor.hasPrev())
        || (hasNextPageInDirection && direction.equals("DESC"))) {
      // Cursor should be the last element in the list.
      nextCursor = results.get(results.size() - 1).getCursor();
    }

    PagedCursor responseCursor = new PagedCursor(prevCursor, nextCursor);
    PagedResults<T> pr = new PagedResults<T>(results, responseCursor);
    return pr;
  }

  StringJoiner generateWhereClauseJoiner(HashMap<String, Field> fieldsMap,
      Map<String, Object> params, String orderByField, PagedCursor requestCursor) {
    StringJoiner whereClause = new StringJoiner(" AND ");

    for (Map.Entry<String, Object> e : params.entrySet()) {
      Field dbField = fieldsMap.getOrDefault(e.getKey(), null);

      // If the params contained a cursor, add the correct WHERE clause to filter the results.
      if (e.getKey().equals("cursor")) {
        String direction = requestCursor != null && requestCursor.hasPrev() ? ">" : "<";
        whereClause.add(String.format("%s %s ?", orderByField, direction));
        continue;
      } else if (dbField == null) {
        // params don't match a known db field, so continue with params.
        continue;
      }
      String field = dbField.getName();
      DatabaseField annotation = dbField.getAnnotation(DatabaseField.class);
      DatabaseListField listAnnotation = dbField.getAnnotation(DatabaseListField.class);
      if (listAnnotation != null) {
        // get all list values we are querying for
        Object listValue = params.get(field);

        if (listValue.equals("")) {
          // This is an empty list filter, ignore it.
          continue;
        }

        // Assemble the resulting WHERE clause using the list value provided.
        StringBuilder result = new StringBuilder();
        if (listValue instanceof String) {
          String[] values = ((String) params.get(field)).split(listAnnotation.seperator());
          int valueIndex = 1;
          for (String value : values) {
            result.append(field).append(" LIKE ").append(String.format("\"%%%s%%\"", value));
            if (valueIndex < values.length) {
              result.append("\n AND ");
            }
            valueIndex++;
          }
        } else {
          throw new IllegalArgumentException(String.format(
              "Query parameter %s was not a handleable field type for the declared field.", field));
        }
        whereClause.add(result.toString());
        continue;
      }

      whereClause.add(String.format("%s%s?", dbField.getName(), annotation.whereOperator()));
    }

    return whereClause;
  }

  /**
   * Generates a list of database queriable fields as a comma seprated list.
   *
   * @param cls                        - The class to inspect.
   * @param includeId                  - whether to include the id field for this
   *                                   entity.
   * @param includeFieldsSetByDatabase - whether to include fields that the
   *                                   database sets.
   * @return fields - Comma seperated list of fields. i.e. id,field1,field2
   */
  private <T extends BaseModel> String getQueryFieldsString(
      Class<T> cls, Boolean includeId, Boolean includeFieldsSetByDatabase) {
    StringJoiner fields = new StringJoiner(",");

    for (Field f : cls.getDeclaredFields()) {
      DatabaseField annotation = f.getAnnotation(DatabaseField.class);
      if (annotation == null || (!includeId && annotation.isId())
          || (!includeFieldsSetByDatabase && annotation.isSetByDatabase())) {
        continue;
      }
      fields.add(f.getName());
    }

    return fields.toString();
  }

  /**
   * Generates a map of Field names to class fields.
   *
   * @param cls       - The class to inspect.
   * @param includeId - whether to include the id field for this entity.
   * @return fields - Map of string name to actual class field.
   */
  private <T extends BaseModel> HashMap<String, Field> getQueryFieldsMap(
      Class<T> cls, Boolean includeId) {
    HashMap<String, Field> fields = new HashMap<String, Field>();

    for (Field f : cls.getDeclaredFields()) {
      DatabaseField annotation = f.getAnnotation(DatabaseField.class);
      if (annotation == null || (!includeId && annotation.isId())) {
        continue;
      }
      fields.put(f.getName(), f);
    }

    return fields;
  }

  /**
   * Generates a list of object values from the given BaseModel entity.
   *
   * @param entity    - The entity to inspect.
   * @param includeId - whether to include the id field for this entity.
   * @return values - List of values for database fields on that entity.
   */
  private <T extends BaseModel> ArrayList<Object> getQueryFieldValues(
      T entity, Boolean includeId, Boolean includeFieldsSetByDatabase) {
    ArrayList<Object> values = new ArrayList<Object>();

    for (Field f : entity.getClass().getDeclaredFields()) {
      DatabaseField annotation = f.getAnnotation(DatabaseField.class);
      if (annotation == null || (!includeId && annotation.isId())
          || (!includeFieldsSetByDatabase && annotation.isSetByDatabase())) {
        continue;
      }

      try {
        values.add(f.get(entity));

      } catch (IllegalAccessException e) {
        log.debug(
            "Field was not accessible, check fields marked with @DatabaseField are not private.",
            e);
      }
    }

    return values;
  }
}
