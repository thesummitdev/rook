/* TODO create database if not exists. */

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE LANGUAGE IF NOT EXISTS "plpsql";

DROP TABLE IF EXISTS links;
DROP TABLE IF EXISTS users;

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
  modified timestamp with time zone     NOT NULL DEFAULT (now() at time zone 'utc'),
  PRIMARY KEY ( id ),
  CONSTRAINT fk_userid FOREIGN KEY ( userId ) REFERENCES users(id)
);

DROP FUNCTION IF EXISTS links_modified_timestamp;
DROP TRIGGER IF EXISTS links_update_trigger on links;

CREATE FUNCTION links_modified_timestamp() RETURNS TRIGGER as $lmt$
  BEGIN
    -- Check that new row exists
    IF NEW is NULL THEN
      RAISE EXCEPTION 'new cannot be null';
    ENDIF;

    NEW.modified := ( now() at time zone 'utc' )
    RETURN NEW;
  END;
$lmt$ LANGUAGE plpsql;

CREATE TRIGGER links_update_trigger
  AFTER UPDATE ON links
  FOR EACH ROW EXECUTE FUNCTION links_modified_timestamp()

