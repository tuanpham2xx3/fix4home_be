-- ========================================
-- Migration: Add Online Status and Location Tracking to Technician Profiles
-- Version: V009
-- Description: Adds online/offline status, location, and working radius fields
-- ========================================

-- Add online status field
ALTER TABLE technician_profiles 
ADD COLUMN is_online BOOLEAN NOT NULL DEFAULT FALSE;

-- Add last seen timestamp
ALTER TABLE technician_profiles 
ADD COLUMN last_seen_at TIMESTAMP NULL;

-- Add current location fields
ALTER TABLE technician_profiles 
ADD COLUMN current_latitude DOUBLE PRECISION NULL;

ALTER TABLE technician_profiles 
ADD COLUMN current_longitude DOUBLE PRECISION NULL;

-- Add current address field
ALTER TABLE technician_profiles 
ADD COLUMN current_address VARCHAR(500) NULL;

-- Add working radius field (in kilometers)
ALTER TABLE technician_profiles 
ADD COLUMN working_radius INTEGER NOT NULL DEFAULT 10;

-- Create index for location-based queries
CREATE INDEX idx_technician_profiles_online_status ON technician_profiles(is_online);
CREATE INDEX idx_technician_profiles_location ON technician_profiles(current_latitude, current_longitude);
CREATE INDEX idx_technician_profiles_last_seen ON technician_profiles(last_seen_at);

-- Add comments for documentation
ALTER TABLE technician_profiles
    MODIFY COLUMN is_online BOOLEAN NOT NULL DEFAULT FALSE
        COMMENT 'Indicates if technician is currently online and available';

ALTER TABLE technician_profiles
    MODIFY COLUMN last_seen_at TIMESTAMP NULL
        COMMENT 'Timestamp when technician was last seen online';

ALTER TABLE technician_profiles
    MODIFY COLUMN current_latitude DOUBLE PRECISION NULL
        COMMENT 'Current latitude coordinate of technician';

ALTER TABLE technician_profiles
    MODIFY COLUMN current_longitude DOUBLE PRECISION NULL
        COMMENT 'Current longitude coordinate of technician';

ALTER TABLE technician_profiles
    MODIFY COLUMN current_address VARCHAR(500) NULL
        COMMENT 'Human-readable current address of technician';

ALTER TABLE technician_profiles
    MODIFY COLUMN working_radius INTEGER NOT NULL DEFAULT 10
        COMMENT 'Working radius in kilometers from current location'; 