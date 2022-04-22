DO
$do$
  BEGIN
    IF NOT EXISTS ( SELECT FROM pg_catalog.pg_user WHERE usename = 'rook_system') THEN
      CREATE USER rook_system WITH PASSWORD 'rooksystem';
    ELSE
      ALTER USER rook_system PASSWORD 'rooksystem';
    END IF;
  END
$do$;
