/* TODO create database if not exists. */

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS links;
CREATE TABLE IF NOT EXISTS links(
  id      uuid            NOT NULL DEFAULT uuid_generate_v4(),
  url     varchar(1000)   NOT NULL,
  tags    varchar(1000)   NULL,
  unread  boolean         NOT NULL DEFAULT false,
  CONSTRAINT links_pkey PRIMARY KEY ( id )
);
