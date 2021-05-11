package dev.thesummit.flink.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface DatabaseObject {
  public void add() throws SQLException;

  public static void delete(String uuid) throws SQLException {}

  public static DatabaseObject get(String uuid) throws SQLException {
    return null;
  }

  public static List<DatabaseObject> getAll() throws SQLException {
    return new ArrayList<DatabaseObject>();
  }

  public void update() throws SQLException;
}
