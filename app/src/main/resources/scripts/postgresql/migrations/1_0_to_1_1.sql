ALTER TABLE system ADD COLUMN id uuid NOT NULL DEFAULT uuid_generate_v4();
ALTER TABLE system DROP CONSTRAINT system_pkey;
ALTER TABLE system ADD PRIMARY KEY (id);
ALTER TABLE system ADD UNIQUE (key);

-- Finally, update the system version.
UPDATE system SET value = '1.1' WHERE key = 'database_schema_version';

