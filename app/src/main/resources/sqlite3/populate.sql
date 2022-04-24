-- Register database_schema_version.
INSERT INTO SYSTEM (key,value) VALUES ('database_schema_version', '1.0') ON CONFLICT DO NOTHING;

