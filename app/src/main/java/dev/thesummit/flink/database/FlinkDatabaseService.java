package dev.thesummit.flink.database;

import com.google.inject.Inject;
import dev.thesummit.flink.models.BaseModel;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlinkDatabaseService implements DatabaseService {

  private static Logger log = LoggerFactory.getLogger(FlinkDatabaseService.class);
  private ConnectionPool pool;
  private HashMap<Class<?>, String> tableMapping;

  @Inject
  public FlinkDatabaseService(ConnectionPool pool, HashMap<Class<?>, String> tableMapping) {
    this.pool = pool;
    this.tableMapping = tableMapping;
  }

  @Override
  public void put(BaseModel entity) {
    String tableName = this.tableMapping.get(entity.getClass());

    String fields = this.getQueryFieldsString(entity.getClass(), /*includeId= */ false);
    ArrayList<Object> values = this.getQueryFieldValues(entity, /*includeId= */ false);
    String valuePlaceholders = "?";
    if (values.size() > 1) {
      valuePlaceholders = "?,".repeat(values.size() - 1) + "?";
    }

    StringBuilder query =
        new StringBuilder(
            String.format(
                "INSERT into %s (%s) VALUES (%s) returning id;",
                tableName, fields, valuePlaceholders));
    log.debug(query.toString());
    log.debug(values.toString());

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

      log.debug("PUT SQL: ", statement.toString());
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          entity.setId(rs.getObject("id", UUID.class));
        }
      }
    } catch (SQLException e) {
      log.debug("Unable to create entity, SQL error occured.", e);
    } finally {

      this.pool.releaseConnection(conn);
    }
  }

  @Override
  public void patch(BaseModel entity) {
    String tableName = this.tableMapping.get(entity.getClass());
    StringBuilder fields = new StringBuilder();

    int fieldIndex = 1;
    UUID id = entity.getId();
    for (Field f : entity.getClass().getDeclaredFields()) {

      DatabaseField annotation = f.getAnnotation(DatabaseField.class);
      if (annotation == null || annotation.ignore() || annotation.isId()) {
        if (annotation.isId()) {
          try {
            id = (UUID) f.get(entity);

          } catch (IllegalAccessException e) {
            log.debug(
                "Field was not accessible, check fields marked with @DatbaseField are not private.",
                e);
            return;
          }
        }

        fieldIndex++;
        continue;
      }
      fields.append(" ");
      fields.append(f.getName());
      fields.append("=?");
      if (fieldIndex != entity.getClass().getDeclaredFields().length) {
        fields.append(",");
        fieldIndex++;
      }
    }

    if (id == null) {
      return;
    }

    StringBuilder query =
        new StringBuilder(
            String.format(
                "UPDATE %s set %s WHERE id::text='%s' returning id;",
                tableName, fields.toString(), id));

    Connection conn = this.pool.getConnection();
    try (PreparedStatement statement = conn.prepareStatement(query.toString())) {

      fieldIndex = 0;
      for (Field f : entity.getClass().getDeclaredFields()) {
        DatabaseField annotation = f.getAnnotation(DatabaseField.class);
        if (annotation == null || annotation.ignore() || annotation.isId()) {
          fieldIndex++;
          continue;
        }
        if (f.getType() == Boolean.class) {
          statement.setBoolean(fieldIndex, (Boolean) f.get(entity));
        } else if (f.getType() == Integer.class) {
          statement.setInt(fieldIndex, f.getInt(entity));
        } else {
          statement.setString(fieldIndex, (String) f.get(entity));
        }
        fieldIndex++;
      }

      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          entity.setId(rs.getObject("id", UUID.class));
        }
      }
    } catch (IllegalAccessException e) {
      log.debug(
          "Field was not accessible, check fields marked with @DatbaseField are not private.", e);
      return;
    } catch (SQLException e) {
      log.debug("Unable to patch entity, SQL error occured.", e);
    } finally {
      this.pool.releaseConnection(conn);
    }
  }

  @Override
  public void delete(BaseModel entity) {

    String tableName = this.tableMapping.get(entity.getClass());
    String query = String.format("DELETE FROM %s where id::text=?;", tableName);

    Connection conn = this.pool.getConnection();

    try (PreparedStatement statement = conn.prepareStatement(query)) {

      statement.setString(1, entity.getId().toString());
      statement.execute();
    } catch (SQLException e) {
      log.debug("Unable to delete entity, SQL error occured.", e);
    } finally {
      this.pool.releaseConnection(conn);
    }
  }

  @Override
  public <T extends BaseModel> T get(Class<T> cls, String identifier) {

    String tableName = this.tableMapping.get(cls);
    Connection conn = this.pool.getConnection();

    String fields = this.getQueryFieldsString(cls, true);
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

    StringBuilder query =
        new StringBuilder(
            String.format(
                "SELECT %s FROM %s WHERE %s='%s';",
                fields, tableName, identifierField, identifier));

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
      return null;
    } finally {
      this.pool.releaseConnection(conn);
    }
    return null;
  }

  @Override
  public <T extends BaseModel> T get(Class<T> cls, UUID id) {

    String tableName = this.tableMapping.get(cls);
    Connection conn = this.pool.getConnection();

    String fields = this.getQueryFieldsString(cls, true);

    StringBuilder query =
        new StringBuilder(
            String.format("SELECT %s FROM %s WHERE id::text='%s';", fields, tableName, id));

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
            return null;
          }
        }
      }
    } catch (SQLException e) {
      return null;
    } finally {
      this.pool.releaseConnection(conn);
    }
    return null;
  }

  @Override
  public <T extends BaseModel> List<T> getAll(Class<T> cls, Map<String, Object> params) {

    String fieldsString = this.getQueryFieldsString(cls, /* includeId= */ true);
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
      DatabaseField annotation = dbField.getAnnotation(DatabaseField.class);

      query.append(field).append(annotation.cast()).append(annotation.whereOperator()).append("?");

      if (index == params.size()) {
        query.append(";");
      } else {
        query.append("\n AND ");
      }
      index++;
    }

    String sql = query.toString();
    log.debug(sql);
    Connection conn = this.pool.getConnection();
    try (PreparedStatement statement = conn.prepareStatement(sql)) {

      index = 1;
      for (Map.Entry<String, Object> e : params.entrySet()) {
        if (e.getValue() instanceof Boolean) {
          boolean b = (boolean) e.getValue();
          statement.setBoolean(index, b);
        } else if (e.getValue() instanceof Integer) {
          int i = (int) e.getValue();
          statement.setInt(index, i);
        } else if (e.getValue() instanceof String) {
          statement.setString(index, e.getValue().toString());
        } else {
          statement.setObject(index, e.getValue());
        }
        index++;
      }

      log.info("GetAll SQL: ", statement.toString());
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          try {
            Method m = cls.getMethod("fromResultSet", ResultSet.class);
            @SuppressWarnings("unchecked") // Cast the Object to T for it to be returned.
            T entity = (T) m.invoke(null, rs);
            results.add(entity);
          } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // Couldn't call fromResultSet
            return null;
          }
        }
      }
    } catch (SQLException e) {
      log.debug("Database error during getAll", e);
      return null;
    } finally {
      this.pool.releaseConnection(conn);
    }

    return results;
  }

  /**
   * Generates a list of database queriable fields as a comma seprated list.
   *
   * @param cls - The class to inspect.
   * @param includeId - whether to include the UUID field for this entity.
   * @return fields - Comma seperated list of fields. i.e. id,field1,field2
   */
  private <T extends BaseModel> String getQueryFieldsString(Class<T> cls, Boolean includeId) {
    StringBuilder fields = new StringBuilder();

    int fieldIndex = 1;
    for (Field f : cls.getDeclaredFields()) {

      DatabaseField annotation = f.getAnnotation(DatabaseField.class);
      if ((annotation == null || annotation.ignore()) || (!includeId && annotation.isId())) {
        fieldIndex++;
        continue;
      }
      fields.append(f.getName());
      if (fieldIndex != cls.getDeclaredFields().length) {
        fields.append(",");
        fieldIndex++;
      }
    }

    return fields.toString();
  }

  /**
   * Generates a list of database queriable fields as a comma seprated list.
   *
   * @param cls - The class to inspect.
   * @param includeId - whether to include the UUID field for this entity.
   * @return fields - Comma seperated list of fields. i.e. id,field1,field2
   */
  private <T extends BaseModel> HashMap<String, Field> getQueryFieldsMap(
      Class<T> cls, Boolean includeId) {
    HashMap<String, Field> fields = new HashMap<String, Field>();

    for (Field f : cls.getDeclaredFields()) {

      DatabaseField annotation = f.getAnnotation(DatabaseField.class);
      if ((annotation == null || annotation.ignore()) || (!includeId && annotation.isId())) {
        continue;
      }
      fields.put(f.getName(), f);
    }

    return fields;
  }

  /**
   * Generates a list of object values from the given BaseModel entity.
   *
   * @param entity - The entity to inspect.
   * @param includeId - whether to include the UUID field for this entity.
   * @return values - List of values for database fields on that entity.
   */
  private <T extends BaseModel> ArrayList<Object> getQueryFieldValues(T entity, Boolean includeId) {

    ArrayList<Object> values = new ArrayList<Object>();

    for (Field f : entity.getClass().getDeclaredFields()) {
      DatabaseField annotation = f.getAnnotation(DatabaseField.class);
      if ((annotation == null || annotation.ignore()) || (!includeId && annotation.isId())) {
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