CREATE TABLE IF NOT EXISTS apikeys(
  id       INTEGER PRIMARY KEY ASC NOT NULL,
  userId   INTEGER                 NOT NULL,
  key      TEXT                    NOT NULL,
  agent    TEXT                    NOT NULL DEFAULT 'unknown',
  created  timestamp(0)            NOT NULL DEFAULT ( datetime('now', 'utc')),
  FOREIGN KEY ( userId ) REFERENCES users(id)
);

-- Finally, update the system version.
PRAGMA user_version=120;
