package dev.thesummit.rook.database;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.thesummit.rook.models.Link;
import dev.thesummit.rook.models.Preference;
import dev.thesummit.rook.models.SystemKey;
import dev.thesummit.rook.models.User;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseModule extends AbstractModule {

  private static Logger log = LoggerFactory.getLogger(DatabaseModule.class);
  static final Map<String, String> env = System.getenv();
  static final String DATA_LOCATION = env.getOrDefault("DATA", "/usr/local/rook/data");
  static final String CONNECTION_STRING = String.format("jdbc:sqlite:%s/rook.db", DATA_LOCATION);
  static RookConnectionPool pool;
  static HashMap<Class<?>, String> tableMapping;
  static DatabaseService service;

  public DatabaseModule(boolean shouldResetDatabase) {
    try {
      DatabaseModule.pool = RookConnectionPool.create(DatabaseModule.CONNECTION_STRING);
    } catch (SQLException e) {
      throw new RuntimeException("Unable to connected to database", e);
    }

    DatabaseModule.tableMapping = new HashMap<Class<?>, String>();
    DatabaseModule.tableMapping.put(Link.class, "LINKS");
    DatabaseModule.tableMapping.put(User.class, "USERS");
    DatabaseModule.tableMapping.put(Preference.class, "PREFERENCES");
    DatabaseModule.tableMapping.put(SystemKey.class, "SYSTEM");

    DatabaseModule.service =
        new RookDatabaseService(DatabaseModule.pool, DatabaseModule.tableMapping);

    initializeDatabase(shouldResetDatabase);
  }

  private void initializeDatabase(boolean shouldResetDatabase) {

    log.info("initializeDatabase");

    // Check if system table exists
    String systemTableExistsQuery =
        "SELECT name from sqlite_master WHERE type='table' and name='system'";

    Connection conn = pool.getConnection();

    try {

      PreparedStatement stmt = conn.prepareStatement(systemTableExistsQuery);
      ResultSet rs = stmt.executeQuery();
      if (rs.next() && !shouldResetDatabase) {
        // Some version of rook database exists, so update versions here.
      } else {
        // No version of the database exists, so init from scratch.
        rs.close();
        stmt.close();

        ScriptRunner runner = new ScriptRunner(conn);
        runner.setAutoCommit(true);

        if (shouldResetDatabase) {
          Reader resetScript =
              new BufferedReader(
                  new InputStreamReader(getClass().getResourceAsStream("/sqlite3/reset.sql")));

          log.info("Dropping all data and re-initializing database.");
          runner.setDelimiter("$");
          runner.runScript(resetScript);
        }

        Reader initDbScript =
            new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/sqlite3/init.sql")));
        Reader populateScript =
            new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/sqlite3/populate.sql")));

        try {
          runner.setDelimiter("$");
          runner.runScript(initDbScript);

          runner.setDelimiter(";");
          runner.runScript(populateScript);

          if (shouldResetDatabase) {
            Preference allowNewUsers = new Preference("allowNewUsers", "true");
            allowNewUsers.setId(UUID.randomUUID());
            service.put(allowNewUsers);
          }

        } catch (Exception e) {
          // TODO: handle exception
          log.info("error", e);
        }
      }
    } catch (SQLException e) {
      log.info("Error getting system table", e);
    } finally {
      pool.releaseConnection(conn);
    }
  }

  @Provides()
  static ConnectionPool provideConnectionPool() {
    return DatabaseModule.pool;
  }

  @Provides()
  static HashMap<Class<?>, String> provideTableMapping() {
    return DatabaseModule.tableMapping;
  }

  @Provides()
  static DatabaseService provideDatabaseService() {
    return DatabaseModule.service;
  }
}
