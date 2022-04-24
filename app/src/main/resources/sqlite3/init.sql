
-- ---------------------- TABLE DEFINITIONS -------------------------------------

CREATE TABLE IF NOT EXISTS users(
  id                    varchar(100)    NOT NULL,
  username              varchar(1000)   NOT NULL,
  userEncryptedPassword varchar(1000)   NULL,
  userSalt              varchar(1000)   NULL,
  PRIMARY KEY ( id )
);$

CREATE TABLE IF NOT EXISTS links(
  id       varchar(100)                 NOT NULL,
  title    varchar(1000)                NOT NULL,
  url      varchar(1000)                NOT NULL,
  tags     varchar(1000)                NULL,
  userId   uuid                         NOT NULL,
  modified timestamp(0)                 NOT NULL DEFAULT ( datetime('now', 'utc') ),
  PRIMARY KEY ( id ),
  FOREIGN KEY ( userId ) REFERENCES users(id)
);$

CREATE TABLE IF NOT EXISTS preferences(
  id        varchar(100)                NOT NULL,
  key       varchar(100)                NOT NULL,
  value     varchar(100)                NOT NULL,
  userId    uuid                        NULL,
  PRIMARY KEY ( id ),
  FOREIGN KEY ( userId ) REFERENCES users(id)
);$
-- Pref for any given user / key combination should be unique.
CREATE UNIQUE INDEX IF NOT EXISTS preferences_key_userid on preferences(key,userid);$

CREATE TABLE IF NOT EXISTS system(
  key       varchar(100)                NOT NULL,
  value     varchar(100)                NOT NULL,
  PRIMARY KEY ( key )
);$

-- ------------------------------------------------------------------------------
--
-- ---------------------- FUNCTION / TRIGGER DEFINITIONS ------------------------

-- Trigger for the links table to update modified timestamp using the above
-- function.
DROP TRIGGER IF EXISTS update_link_modified;$
CREATE TRIGGER update_link_modified AFTER UPDATE ON links
FOR EACH ROW
  BEGIN
    UPDATE links set modified = (datetime('now', 'utc')) WHERE id = old.id;
  END;$

-- ------------------------------------------------------------------------------
