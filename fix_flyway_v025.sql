-- Script to fix Flyway V025 checksum mismatch
-- Run this script in MySQL to remove the old V025 migration record

USE fix4home_db;

-- Delete the old V025 migration record from flyway_schema_history
DELETE FROM flyway_schema_history 
WHERE version = '025' 
  AND checksum = -2046128863;

-- Verify deletion
SELECT * FROM flyway_schema_history WHERE version = '025';

