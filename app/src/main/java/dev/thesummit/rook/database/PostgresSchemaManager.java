package dev.thesummit.rook.database;

import com.google.inject.Inject;
import dev.thesummit.rook.models.SystemKey;
import dev.thesummit.rook.utils.Flag;
import dev.thesummit.rook.utils.FlagService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** PostgresSchemaManager */
public class PostgresSchemaManager implements DatabaseSchemaManager {

  private static Logger log = LoggerFactory.getLogger(PostgresSchemaManager.class);

  private static final String VERSION_1_0 = "1.0";
  private static final String VERSION_1_1 = "1.1";

  private static final String RELEASE_VERSION = "1.1";

  private ConnectionPool pool;
  private DatabaseService dbService;

  private Flag resetFlag;
  private Flag seedTestData;

  @Inject
  public PostgresSchemaManager(DatabaseService dbService, FlagService flags, ConnectionPool pool) {
    this.resetFlag = flags.defineFlag("resetdb", false);
    this.seedTestData = flags.defineFlag("seedtestdata", false);
    this.pool = pool;
    this.dbService = dbService;
  }

  @Override
  public void verifySchema() {

    Connection connection = pool.getConnection();
    ScriptRunner runner = new ScriptRunner(connection);
    runner.setSendFullScript(true);

    if (resetFlag.isEnabled()) {
      log.info("Database reset was requested...");
      resetDatabase(runner);
    }
    // Verify a valid rook database exists
    if (!systemTableExists()) {
      // No valid database, init the database from scratch.
      BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(
                  getClass().getResourceAsStream("/scripts/postgresql/schema_init.sql")));

      BufferedReader populate =
          new BufferedReader(
              new InputStreamReader(
                  getClass().getResourceAsStream("/scripts/postgresql/populate.sql")));

      runner.runScript(reader);
      runner.runScript(populate);
    }
    // Check for system version
    SystemKey systemReportedVersion = dbService.get(SystemKey.class, "database_schema_version");
    // Check for incremental schema updates.
    if (upgradeRequired(systemReportedVersion.value)) {
      log.info("System upgrade required.");

      if (systemReportedVersion.value.equals(PostgresSchemaManager.VERSION_1_0)) {
        BufferedReader reader1_0_To_1_1 =
            new BufferedReader(
                new InputStreamReader(
                    getClass()
                        .getResourceAsStream("/scripts/postgresql/migrations/1_0_to_1_1.sql")));
        runner.runScript(reader1_0_To_1_1);
      }
    } else {
      log.info("Rook database is up to date!");
    }

    if (seedTestData.isEnabled()) {
      log.info("Seeding with test data");
      BufferedReader testData =
          new BufferedReader(
              new InputStreamReader(
                  getClass().getResourceAsStream("/scripts/postgresql/test_data.sql")));
      runner.runScript(testData);
    }
  }

  /**
   * Checks the binary RELEASE_VERSION against the systemReportedVersion to see if a schema upgrade
   * is required.
   *
   * @param systemReportedVersion - the current version reported by the database.
   * @return whether the database requires an upgrade.
   */
  private Boolean upgradeRequired(String systemReportedVersion) {

    log.info(
        String.format("checking for required upgrade, current verison: %s", systemReportedVersion));
    if (!systemReportedVersion.contains(".")) {
      log.info("Could not fetch valid schema version string: ", systemReportedVersion);
    }

    String majorVersion = systemReportedVersion.split("\\.")[0];
    String minorVersion = systemReportedVersion.split("\\.")[1];
    String currentMajorVersion = PostgresSchemaManager.RELEASE_VERSION.split("\\.")[0];
    String currentMinorVersion = PostgresSchemaManager.RELEASE_VERSION.split("\\.")[1];
    log.info(
        String.format(
            "Current: %s.%s Next: %s.%s",
            majorVersion, minorVersion, currentMajorVersion, currentMinorVersion));

    return !(majorVersion.equals(currentMinorVersion) && minorVersion.equals(currentMinorVersion));
  }

  /** Runs the reset_schema.sql file against the currently connected database. */
  private void resetDatabase(ScriptRunner runner) {

    BufferedReader resetScript =
        new BufferedReader(
            new InputStreamReader(
                getClass().getResourceAsStream("/scripts/postgresql/reset_schema.sql")));

    runner.runScript(resetScript);
  }

  /**
   * Runs a Query against the currently connected database to see if the `SYSTEM` database exists.
   *
   * @return exists
   */
  private Boolean systemTableExists() {

    Connection connection = pool.getConnection();
    String systemTableExistsQuery =
        "SELECT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename ="
            + " 'system');";

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
}
