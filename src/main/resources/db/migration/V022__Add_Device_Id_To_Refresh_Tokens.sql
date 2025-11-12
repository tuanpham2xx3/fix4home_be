-- V022: Add device_id and last_used_at columns to refresh_tokens table
-- Date: 2025-11-12
-- Description: Add device_id column to support multi-device refresh token management
--              and last_used_at to track token usage

-- Add device_id column (nullable first to handle existing data)
ALTER TABLE refresh_tokens 
ADD COLUMN device_id VARCHAR(200) NULL COMMENT 'Device identifier for multi-device support';

-- Add last_used_at column
ALTER TABLE refresh_tokens 
ADD COLUMN last_used_at TIMESTAMP NULL COMMENT 'Last time the refresh token was used';

-- Delete existing refresh tokens without device_id (they are invalid for new system)
-- This is safe because refresh tokens are temporary and can be regenerated
DELETE FROM refresh_tokens WHERE device_id IS NULL;

-- Now make device_id NOT NULL since we've cleaned up old data
ALTER TABLE refresh_tokens 
MODIFY COLUMN device_id VARCHAR(200) NOT NULL COMMENT 'Device identifier for multi-device support';

-- Add unique constraint for user_id + device_id combination
-- This ensures one refresh token per user per device
ALTER TABLE refresh_tokens 
ADD CONSTRAINT uk_user_device UNIQUE (user_id, device_id);

-- Add index for device_id queries
CREATE INDEX idx_refresh_tokens_device_id ON refresh_tokens(device_id);

-- Add index for last_used_at queries
CREATE INDEX idx_refresh_tokens_last_used_at ON refresh_tokens(last_used_at);

