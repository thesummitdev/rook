BEGIN TRANSACTION;

CREATE TABLE IF NOT EXISTS users_backup(
  id                    INTEGER PRIMARY KEY ASC NOT NULL,
  username              TEXT                    NOT NULL,
  userEncryptedPassword TEXT                    NULL,
  userSalt              TEXT                    NULL
);

INSERT INTO users_backup 
  SELECT id, username, userEncryptedPassword, userSalt from users;

DROP TABLE users;

ALTER TABLE users_backup RENAME to users;

COMMIT;

-- Finally, update the system version.
PRAGMA user_version=100;
