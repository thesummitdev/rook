DO
$do$
  BEGIN
    IF NOT EXISTS ( SELECT FROM pg_catalog.pg_user WHERE usename = 'flink_system') THEN
      CREATE USER flink_system WITH PASSWORD 'flinksystem';
    ELSE
      ALTER USER flink_system PASSWORD 'flinksystem';
    END IF;
  END
$do$;
