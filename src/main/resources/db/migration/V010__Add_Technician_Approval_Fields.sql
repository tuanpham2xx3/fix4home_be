-- ========================================
-- Migration: Add Technician Approval Process Fields
-- Version: V010
-- Description: Adds verification documents, rejection reason, approved date and admin fields for technician approval process
-- ========================================

-- Add verification documents field
ALTER TABLE technician_profiles 
ADD COLUMN verification_documents VARCHAR(1000) NULL;

-- Add rejection reason field
ALTER TABLE technician_profiles 
ADD COLUMN rejection_reason VARCHAR(500) NULL;

-- Add approved timestamp
ALTER TABLE technician_profiles 
ADD COLUMN approved_at TIMESTAMP NULL;

-- Add approved by admin field
ALTER TABLE technician_profiles 
ADD COLUMN approved_by BIGINT NULL;

-- Update existing ACTIVE technicians to PENDING_APPROVAL status
-- This ensures proper workflow for existing data
UPDATE technician_profiles 
SET status = 'PENDING_APPROVAL' 
WHERE status = 'ACTIVE';

-- Create index for approval queries
CREATE INDEX idx_technician_profiles_status ON technician_profiles(status);
CREATE INDEX idx_technician_profiles_approved_by ON technician_profiles(approved_by);
CREATE INDEX idx_technician_profiles_approved_at ON technician_profiles(approved_at);

-- Add comments for documentation
COMMENT ON COLUMN technician_profiles.verification_documents IS 'URL or path to verification documents uploaded by technician';
COMMENT ON COLUMN technician_profiles.rejection_reason IS 'Reason provided by admin when rejecting technician application';
COMMENT ON COLUMN technician_profiles.approved_at IS 'Timestamp when technician was approved by admin';
COMMENT ON COLUMN technician_profiles.approved_by IS 'User ID of admin who approved the technician';

-- Add foreign key constraint for approved_by field (references users table)
ALTER TABLE technician_profiles 
ADD CONSTRAINT fk_technician_profiles_approved_by 
FOREIGN KEY (approved_by) REFERENCES users(id); 