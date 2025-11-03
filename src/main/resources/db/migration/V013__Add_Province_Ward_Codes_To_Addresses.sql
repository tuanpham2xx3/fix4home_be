-- ========================================
-- Migration: Add Province and Ward Codes to Addresses Table
-- Version: V013
-- Description: Adds province_code and ward_code fields for Vietnam Administrative API integration
-- ========================================

-- Add province code field
ALTER TABLE addresses 
ADD COLUMN province_code VARCHAR(10) NULL;

-- Add ward code field  
ALTER TABLE addresses 
ADD COLUMN ward_code VARCHAR(10) NULL;

-- Create index for province-based address queries
CREATE INDEX idx_addresses_province_code ON addresses(province_code);

-- Create index for ward-based address queries
CREATE INDEX idx_addresses_ward_code ON addresses(ward_code);

-- Create composite index for province-ward lookup
CREATE INDEX idx_addresses_province_ward ON addresses(province_code, ward_code);

-- Add comments for documentation
COMMENT ON COLUMN addresses.province_code IS 'Vietnam Administrative API province code (e.g., "01" for Hà Nội)';
COMMENT ON COLUMN addresses.ward_code IS 'Vietnam Administrative API ward/district code for precise location identification'; 