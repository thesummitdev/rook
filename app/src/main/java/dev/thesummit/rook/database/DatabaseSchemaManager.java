package dev.thesummit.rook.database;

/** DatabaseSchemaManager */
public interface DatabaseSchemaManager {
  public void verifySchema();
  public int fetchDatabaseVersion();
  public void setDatabaseVersion(int version);
}
