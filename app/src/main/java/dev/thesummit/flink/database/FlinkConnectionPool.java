package dev.thesummit.flink.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

public class FlinkConnectionPool implements ConnectionPool {

  private static Logger log = Logger.getLogger(FlinkConnectionPool.class.getName());

  private String url;
  private String user;
  private String password;
  private ArrayList<Connection> connectionPool;
  private ArrayList<Connection> usedConnections = new ArrayList<Connection>(INITIAL_POOL_SIZE);
  private static int INITIAL_POOL_SIZE = 5;
  private static int MAX_POOL_SIZE = 10;

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

  /**
   * Get a jdbc connection to the database.
   *
   * <p>Attempts to reuse existing connections from the connection pool. If there are no available
   * connectiosn to reuse, will create a new connection and add it to the pool.
   *
   * @return a database Connection
   * @throws RuntimeException no connections currently available in the pool and a new connection
   *     cannot be opened.
   */
  @Override
  public Connection getConnection() {
    if (this.connectionPool.size() == 0) {
      try {
        log.info("No available pool connections, attempting to create a new connection...");
        this.connectionPool.add(createConnection(this.url, this.user, this.password));
      } catch (SQLException e) {
        throw new RuntimeException(
            "No available pool connections & cannot create a new connection.", e);
      }
    }
    Connection connection = connectionPool.remove(connectionPool.size() - 1);
    return connection;
  }

  /**
   * Return a jdbc connection to the connections pool.
   *
   * <p>Add a connection that is no longer needed back to the pool of available connections so it
   * can be reused.
   *
   * @param connection The connection to return to the pool.
   * @return success Whether the connection was added back to the pool.
   * @throws RuntimeException - if the connection is not needed for the pool (exceeds
   *     MAX_POOL_CONNECTIONS) and cannot be closed.
   */
  @Override
  public boolean releaseConnection(Connection connection) {
    if (this.connectionPool.size() >= this.MAX_POOL_SIZE) {
      try {
        log.info("ConnectionPool size exceeded, closing connection rather than returning to pool.");
        connection.close();
      } catch (SQLException e) {
        throw new RuntimeException("Unable to close unneeded database connection", e);
      }
      return true;
    }
    connectionPool.add(connection);
    return usedConnections.remove(connection);
  }

  /**
   * Creates a new jdbc connection.
   *
   * @param url The connection url.
   * @param user the username to authenticate with.
   * @param password the password to authenticate with.
   * @return The JDBC connection object.
   * @throws SQLException if a JDBC connection is not successfully made.
   */
  private static Connection createConnection(String url, String user, String password)
      throws SQLException {
    Properties connectionProps = new Properties();
    connectionProps.put("user", user);
    connectionProps.put("password", password);

    return DriverManager.getConnection(url, connectionProps);
  }

  /**
   * Returns to total number of connections in the pool. (Both used and unused connections);
   *
   * @return Count total number of active connections (used + unused)
   */
  public int getSize() {
    return connectionPool.size() + usedConnections.size();
  }

  /**
   * Returns the connection string for this ConnectionPool.
   *
   * @return The connection string.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Returns the user string for this ConnectionPool.
   *
   * @return The user string.
   */
  public String getUser() {
    return user;
  }

  /**
   * Returns the password string for this ConnectionPool.
   *
   * @return The user string.
   */
  public String getPassword() {
    return password;
  }
}
