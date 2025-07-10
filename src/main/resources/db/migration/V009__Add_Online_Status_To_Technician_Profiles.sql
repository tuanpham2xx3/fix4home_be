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
COMMENT ON COLUMN technician_profiles.is_online IS 'Indicates if technician is currently online and available';
COMMENT ON COLUMN technician_profiles.last_seen_at IS 'Timestamp when technician was last seen online';
COMMENT ON COLUMN technician_profiles.current_latitude IS 'Current latitude coordinate of technician';
COMMENT ON COLUMN technician_profiles.current_longitude IS 'Current longitude coordinate of technician';
COMMENT ON COLUMN technician_profiles.current_address IS 'Human-readable current address of technician';
COMMENT ON COLUMN technician_profiles.working_radius IS 'Working radius in kilometers from current location'; 