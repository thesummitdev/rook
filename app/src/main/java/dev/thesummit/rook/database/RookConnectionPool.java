package dev.thesummit.rook.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class RookConnectionPool implements ConnectionPool {

  private static Logger log = Logger.getLogger(RookConnectionPool.class.getName());

  private String path;
  private ArrayList<Connection> connectionPool;
  private ArrayList<Connection> usedConnections = new ArrayList<Connection>(INITIAL_POOL_SIZE);
  private static int INITIAL_POOL_SIZE = 5;
  private static int MAX_POOL_SIZE = 10;

  public static RookConnectionPool create(String path) throws SQLException {
    ArrayList<Connection> pool = new ArrayList<Connection>();
    for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
      pool.add(createConnection(path));
    }
    return new RookConnectionPool(path, pool);
  }

  public RookConnectionPool(String path, ArrayList<Connection> pool) {
    this.path = path;
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
    log.info(String.format("Current connection pool size: %s", this.connectionPool.size()));
    if (this.connectionPool.size() == 0) {
      try {
        log.info("No available pool connections, attempting to create a new connection...");
        this.connectionPool.add(createConnection(this.path));
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
    if (this.connectionPool.size() >= MAX_POOL_SIZE) {
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
   * @param path The connection path.
   * @return The JDBC connection object.
   * @throws SQLException if a JDBC connection is not successfully made.
   */
  private static Connection createConnection(String path) throws SQLException {
    return DriverManager.getConnection(path);
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
   * Returns the connection path for this ConnectionPool.
   *
   * @return The connection path.
   */
  public String getPath() {
    return path;
  }
}
