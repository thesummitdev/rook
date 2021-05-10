package dev.thesummit.flink.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class FlinkConnectionPool implements ConnectionPool {

  private String url;
  private String user;
  private String password;
  private ArrayList<Connection> connectionPool;
  private ArrayList<Connection> usedConnections = new ArrayList<Connection>(INITIAL_POOL_SIZE);
  private static int INITIAL_POOL_SIZE = 10;

  public static FlinkConnectionPool create(String url, String user, String password)
      throws SQLException {
    ArrayList<Connection> pool = new ArrayList<Connection>();
    for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
      pool.add(createConnection(url, user, password));
    }
    return new FlinkConnectionPool(url, user, password, pool);
  }

  public FlinkConnectionPool(String url, String user, String password, ArrayList pool) {
    this.url = url;
    this.user = user;
    this.password = password;
    this.connectionPool = pool;
  }

  @Override
  public Connection getConnection() {
    Connection connection = connectionPool.remove(connectionPool.size() - 1);
    usedConnections.add(connection);
    return connection;
  }

  @Override
  public boolean releaseConnection(Connection connection) {
    connectionPool.add(connection);
    return usedConnections.remove(connection);
  }

  private static Connection createConnection(String url, String user, String password)
      throws SQLException {
    Properties connectionProps = new Properties();
    connectionProps.put("user", user);
    connectionProps.put("password", password);

    return DriverManager.getConnection(url, connectionProps);
  }

  public int getSize() {
    return connectionPool.size() + usedConnections.size();
  }

  public String getUrl() {
    return url;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }
}
