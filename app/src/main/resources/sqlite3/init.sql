
-- ---------------------- TABLE DEFINITIONS -------------------------------------

CREATE TABLE IF NOT EXISTS users(
  id                    INTEGER PRIMARY KEY ASC NOT NULL,
  username              TEXT                    NOT NULL,
  userEncryptedPassword TEXT                    NULL,
  userSalt              TEXT                    NULL
);

CREATE TABLE IF NOT EXISTS links(
  id       INTEGER PRIMARY KEY ASC NOT NULL,
  title    TEXT                    NOT NULL,
  url      TEXT                    NOT NULL,
  tags     TEXT                    NULL,
  userId   INTEGER                 NOT NULL,
  modified timestamp(0)            NOT NULL DEFAULT ( datetime('now', 'utc') ),
  FOREIGN KEY ( userId ) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS preferences(
  id        INTEGER PRIMARY KEY ASC NOT NULL,
  key       TEXT                    NOT NULL,
  value     TEXT                    NOT NULL,
  userId    INTEGER                 NULL,
  FOREIGN KEY ( userId ) REFERENCES users(id)
);
-- Pref for any given user / key combination should be unique.
CREATE UNIQUE INDEX IF NOT EXISTS preferences_key_userid on preferences(key,userid);

CREATE TABLE IF NOT EXISTS system(
  id        INTEGER PRIMARY KEY ASC NOT NULL,
  key       TEXT                    NOT NULL,
  value     TEXT                    NOT NULL
);

-- ------------------------------------------------------------------------------
INSERT INTO SYSTEM (key,value) VALUES ('sqlite3_schema_version', '1.0') ON CONFLICT DO NOTHING;
INSERT INTO PREFERENCES (key, value) VALUES ('allowNewUsers', 'true') ON CONFLICT DO NOTHING;
