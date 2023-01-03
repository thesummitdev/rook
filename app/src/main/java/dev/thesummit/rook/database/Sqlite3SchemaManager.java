package dev.thesummit.rook.database;

import com.google.inject.Inject;
import dev.thesummit.rook.utils.Flag;
import dev.thesummit.rook.utils.FlagService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sqlite3SchemaManager implements DatabaseSchemaManager {

  private static Logger log = LoggerFactory.getLogger(Sqlite3SchemaManager.class);

  private static final int VERSION_DEFAULT = 0;
  private static final int VERSION_100 = 100;
  private static final int VERSION_110 = 110;
  private static final int VERSION_120 = 120;
  private static final String COLON_DELIMITER = ";";
  private static final String SPECIAL_DELIMITER = "$$";
  private static final String USER_VERSION_FETCH = "PRAGMA user_version;";
  private static final String USER_VERSION_SET = "PRAGMA user_version=%s;";

  private static final int RELEASE_VERSION = VERSION_120;

  private final HashMap<Integer, String> SCRIPT_UPGRADE_MAP = new HashMap<Integer, String>();

  private ConnectionPool pool;

  private Flag resetFlag;
  private Flag seedTestData;

  @Inject
  public Sqlite3SchemaManager(FlagService flags, ConnectionPool pool) {
    this.pool = pool;

    this.resetFlag = flags.defineFlag("resetdb", false);
    this.seedTestData = flags.defineFlag("seedtestdata", false);

    // Register upgrade scripts.
    SCRIPT_UPGRADE_MAP.put(VERSION_DEFAULT, "/sqlite3/init.sql");
    SCRIPT_UPGRADE_MAP.put(VERSION_110, "/sqlite3/migrations/100_to_110.sql");
    SCRIPT_UPGRADE_MAP.put(VERSION_120, "/sqlite3/migrations/110_to_120.sql");
  }

  @Override
  public void verifySchema() {

    if (resetFlag.isEnabled() || seedTestData.isEnabled()) {
      log.info("Database reset was requested.");
      runScript("/sqlite3/reset.sql", COLON_DELIMITER);
      runScript("/sqlite3/init.sql", COLON_DELIMITER);
      runScript("/sqlite3/init_triggers.sql", SPECIAL_DELIMITER);
    }

    // if test data was requested when Rook server was launched.
    if (seedTestData.isEnabled()) {
      log.info("Seeding database with test data.");
      runScript("/sqlite3/test_data.sql", COLON_DELIMITER);
    }

    int reportedVersion = fetchDatabaseVersion();

    // Loop over the required upgrades until we reach the release version.
    while (upgradeRequired(reportedVersion)) {
      doUpgrade(reportedVersion);
      // Fetch the new version from the upgraded database.
      reportedVersion = fetchDatabaseVersion();
    }
  }

  private void doUpgrade(int reportedVersion) {

    switch (reportedVersion) {
      case VERSION_DEFAULT:
        // No valid schema for the current connected database.
        // Jump straight to the release version (initdb.sql)
        log.info("No schema version info found, creating rook schema...");
        runScript(SCRIPT_UPGRADE_MAP.get(VERSION_DEFAULT), COLON_DELIMITER);
        runScript("/sqlite3/init_triggers.sql", SPECIAL_DELIMITER);
        break;
      case VERSION_100:
        runScript(SCRIPT_UPGRADE_MAP.get(VERSION_110), COLON_DELIMITER);
        break;
      case VERSION_110:
        runScript(SCRIPT_UPGRADE_MAP.get(VERSION_120), COLON_DELIMITER);
        break;
      default:
        break;
    }
  }

  private void runScript(String path, String delimiter) {

    Connection connection = pool.getConnection();
    ScriptRunner runner = new ScriptRunner(connection);
    runner.setDelimiter(delimiter);
    runner.setSendFullScript(false);
    runner.setEscapeProcessing(false);
    runner.setAutoCommit(true);

    try (BufferedReader script = getReaderForResourcePath(path)) {
      runner.runScript(script);
      pool.releaseConnection(connection);
      Thread.sleep(1000);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /** Builds a Buffered reader for the file at the resource path */
  private BufferedReader getReaderForResourcePath(String path) {
    return new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)));
  }

  /**
   * Checks the binary RELEASE_VERSION to see if a schema upgrade is required.
   *
   * @param version - the current version reported by the database.
   * @return whether the database requires an upgrade.
   */
  private Boolean upgradeRequired(int version) {

    log.info(String.format("checking for required upgrade, current version: %s target: %s", version, RELEASE_VERSION));

    return !(version == RELEASE_VERSION);
  }

  @Override
  public int fetchDatabaseVersion() {
    Connection connection = pool.getConnection();
    try (Statement stmt = connection.createStatement()) {
      try (ResultSet rs = stmt.executeQuery(USER_VERSION_FETCH)) {
        if (rs.next()) {
          return Integer.parseInt(rs.getString(1));
        } else {
          return 0;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeSqlException(e);
    } finally {
      pool.releaseConnection(connection);
    }

  }

  @Override
  public void setDatabaseVersion(int version) {
    Connection connection = pool.getConnection();
    String query = String.format(USER_VERSION_SET, version);
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      stmt.execute();
    } catch (SQLException e) {
      throw new RuntimeSqlException(e);
    } finally {
      pool.releaseConnection(connection);
    }
  }
}
