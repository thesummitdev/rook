package dev.thesummit.rook.database;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.thesummit.rook.models.ApiKey;
import dev.thesummit.rook.models.Link;
import dev.thesummit.rook.models.Preference;
import dev.thesummit.rook.models.SystemKey;
import dev.thesummit.rook.models.User;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
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
      throw new RuntimeException("Unable to connect to database", e);
    }

    DatabaseModule.tableMapping = new HashMap<Class<?>, String>();
    DatabaseModule.tableMapping.put(Link.class, "LINKS");
    DatabaseModule.tableMapping.put(User.class, "USERS");
    DatabaseModule.tableMapping.put(Preference.class, "PREFERENCES");
    DatabaseModule.tableMapping.put(SystemKey.class, "SYSTEM");
    DatabaseModule.tableMapping.put(ApiKey.class, "APIKEYS");

    DatabaseModule.service =
        new RookDatabaseService(DatabaseModule.pool, DatabaseModule.tableMapping);
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
