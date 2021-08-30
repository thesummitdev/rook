/* TODO create database if not exists. */

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

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
  id      uuid            NOT NULL DEFAULT uuid_generate_v4(),
  url     varchar(1000)   NOT NULL,
  tags    varchar(1000)   NULL,
  unread  boolean         NOT NULL DEFAULT false,
  userId  uuid            NOT NULL,
  PRIMARY KEY ( id ),
  CONSTRAINT fk_userid FOREIGN KEY ( userId ) REFERENCES users(id)
);

