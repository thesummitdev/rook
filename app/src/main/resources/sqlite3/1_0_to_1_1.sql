CREATE TABLE IF NOT EXISTS apikeys(
  id       INTEGER PRIMARY KEY ASC NOT NULL,
  userId   INTEGER                 NOT NULL,
  key      TEXT                    NOT NULL,
  created  timestamp(0)            NOT NULL DEFAULT ( datetime('now', 'utc')),
  FOREIGN KEY ( userId ) REFERENCES users(id)
);

UPDATE SYSTEM set value = "1.1" where key = 'sqlite3_schema_version';
