-- Wrapper that simply contains the same sample data used for local/dev
-- Placed here so MySQL initializes with seed data automatically

SOURCE /docker-entrypoint-initdb.d/_sample_payload.sql;


