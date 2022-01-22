CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ---------------------- TABLE DEFINITIONS -------------------------------------

CREATE TABLE IF NOT EXISTS users(
  id                    uuid            NOT NULL DEFAULT uuid_generate_v4(),
  username              varchar(1000)   NOT NULL,
  userEncryptedPassword varchar(1000)   NULL,
  userSalt              varchar(1000)   NULL,
  PRIMARY KEY ( id )
);

CREATE TABLE IF NOT EXISTS links(
  id       uuid                         NOT NULL DEFAULT uuid_generate_v4(),
  title    varchar(1000)                NOT NULL,
  url      varchar(1000)                NOT NULL,
  tags     varchar(1000)                NULL,
  userId   uuid                         NOT NULL,
  modified timestamp(0) with time zone  NOT NULL DEFAULT (now()),
  PRIMARY KEY ( id ),
  CONSTRAINT fk_userid FOREIGN KEY ( userId ) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS preferences(
  id        uuid                        NOT NULL default uuid_generate_v4(),
  key       varchar(100)                NOT NULL,
  value     varchar(100)                NOT NULL,
  userId    uuid                        NULL,
  PRIMARY KEY ( id ),
  CONSTRAINT fk_userid FOREIGN KEY ( userId ) REFERENCES users(id),
  UNIQUE (key, userId) -- Pref for any given user / key combination should be unique.
);

CREATE TABLE IF NOT EXISTS system(
  key       varchar(100)                NOT NULL,
  value     varchar(100)                NOT NULL,
  PRIMARY KEY ( key )
);

-- ------------------------------------------------------------------------------

-- ---------------------- FUNCTION / TRIGGER DEFINITIONS ------------------------

-- First drop the function and trigger so it can be re-added with any updates.
DROP TRIGGER IF EXISTS links_update_trigger on links;
DROP FUNCTION IF EXISTS links_modified_timestamp;

-- Function that automatically updates the modified field with the current
-- utc timestamp.
-- Requires plpgsql which should be installed by default.
CREATE FUNCTION links_modified_timestamp() RETURNS TRIGGER as $lmt$
  BEGIN
    -- Check that new row exists
    IF NEW is NULL THEN
      RAISE EXCEPTION 'new cannot be null';
    END IF;

    NEW.modified = now(); -- use db timezone, but this should be stored as UTC.
    RETURN NEW;
  END;
$lmt$ LANGUAGE plpgsql;

-- Trigger for the links table to update modified timestamp using the above
-- function.
CREATE TRIGGER links_update_trigger
  BEFORE UPDATE ON links
  FOR EACH ROW EXECUTE FUNCTION links_modified_timestamp();

-- ------------------------------------------------------------------------------
GRANT ALL privileges on DATABASE flink to flink_system;
GRANT ALL privileges on ALL TABLES IN SCHEMA public to flink_system;
