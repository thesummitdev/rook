package dev.thesummit.rook.database;

import com.google.inject.Inject;
import dev.thesummit.rook.models.SystemKey;
import dev.thesummit.rook.utils.Flag;
import dev.thesummit.rook.utils.FlagService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sqlite3SchemaManager implements DatabaseSchemaManager {

  private static Logger log = LoggerFactory.getLogger(Sqlite3SchemaManager.class);

  private static final String VERSION_1_0 = "1.0";
  private static final String VERSION_1_1 = "1.1";
  private static final String COLON_DELIMITER = ";";
  private static final String SPECIAL_DELIMITER = "$$";

  private static final String RELEASE_VERSION = VERSION_1_1;

  private final HashMap<String, String> SCRIPT_UPGRADE_MAP = new HashMap<String, String>();

  private DatabaseService dbService;
  private ConnectionPool pool;

  private Flag resetFlag;
  private Flag seedTestData;

  @Inject
  public Sqlite3SchemaManager(FlagService flags, DatabaseService dbService, ConnectionPool pool) {
    this.dbService = dbService;
    this.pool = pool;

    this.resetFlag = flags.defineFlag("resetdb", false);
    this.seedTestData = flags.defineFlag("seedtestdata", false);

    // Register upgrade scripts.
    SCRIPT_UPGRADE_MAP.put(RELEASE_VERSION, "/sqlite3/init.sql");
    SCRIPT_UPGRADE_MAP.put(VERSION_1_1, "/sqlite3/migrations/1_0_to_1_1.sql");
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

    // Verify a valid rook database exists
    if (!systemTableExists()) {
      log.info("No system table found, creating rook schema...");
      // No valid schema for the current connected database.
      // Jump straight to the release version (initdb.sql)
      runScript(SCRIPT_UPGRADE_MAP.get(RELEASE_VERSION), COLON_DELIMITER);
    }

    // The system table should exist by this point, either newly initialized or already existing.
    SystemKey reportedVersion = dbService.get(SystemKey.class, "sqlite3_schema_version");

    // Loop over the required upgrades until we reach the release version.
    while (upgradeRequired(reportedVersion.value)) {
      doUpgrade(reportedVersion);
      // Fetch the new version from the upgraded database.
      reportedVersion = dbService.get(SystemKey.class, "sqlite3_schema_version");
    }
  }

  private void doUpgrade(SystemKey reportedVersion) {

    switch (reportedVersion.value) {
      case VERSION_1_0:
        runScript(SCRIPT_UPGRADE_MAP.get(VERSION_1_1), COLON_DELIMITER);
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
   * Runs a Query against the currently connected database to see if the `SYSTEM` database exists.
   *
   * @return exists
   */
  private Boolean systemTableExists() {

    Connection connection = pool.getConnection();
    String systemTableExistsQuery =
        "SELECT exists (SELECT 1 from sqlite_master WHERE type='table' and name='system')";

    Boolean systemTableExists = false;
    try (Statement stmt = connection.createStatement()) {
      try (ResultSet rs = stmt.executeQuery(systemTableExistsQuery)) {

        if (rs.next()) {
          systemTableExists = rs.getBoolean(1);
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      pool.releaseConnection(connection);
    }
    log.info(String.format("System table exists: %s", systemTableExists));

    return systemTableExists;
  }

  /**
   * Checks the binary RELEASE_VERSION to see if a schema upgrade is required.
   *
   * @param version - the current version reported by the database.
   * @return whether the database requires an upgrade.
   */
  private Boolean upgradeRequired(String version) {

    log.info(String.format("checking for required upgrade, current verison: %s", version));
    if (!version.contains(".")) {
      log.info("Could not fetch valid schema version string: ", version);
    }

    String majorVersion = version.split("\\.")[0];
    String minorVersion = version.split("\\.")[1];
    String currentMajorVersion = Sqlite3SchemaManager.RELEASE_VERSION.split("\\.")[0];
    String currentMinorVersion = Sqlite3SchemaManager.RELEASE_VERSION.split("\\.")[1];
    log.info(
        String.format(
            "Current: %s.%s Next: %s.%s",
            majorVersion, minorVersion, currentMajorVersion, currentMinorVersion));

    return !(majorVersion.equals(currentMajorVersion) && minorVersion.equals(currentMinorVersion));
  }
}
