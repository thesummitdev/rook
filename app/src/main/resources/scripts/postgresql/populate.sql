-- Register database_schema_version.
INSERT INTO SYSTEM (key,value) VALUES ('database_schema_version', '1.0') ON CONFLICT DO NOTHING;

-- Check for existing prefs before adding the defaults.
DO
$do$
  BEGIN
    -- appVersion
    IF NOT EXISTS (SELECT * FROM PREFERENCES where key = 'appVersion') THEN
      INSERT INTO PREFERENCES (key, value) VALUES ('appVersion', '1.0');
    END IF;

    -- allowNewUsers
    IF NOT EXISTS (SELECT * FROM PREFERENCES where key = 'allowNewUsers') THEN
      INSERT INTO PREFERENCES (key, value) VALUES ('allowNewUsers', 'true');
    END IF;

  END;
$do$
