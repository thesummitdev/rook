package dev.thesummit.rook.database;

import java.sql.Connection;

public interface ConnectionPool {
  Connection getConnection();

  boolean releaseConnection(Connection connection);

  String getPath();
}
