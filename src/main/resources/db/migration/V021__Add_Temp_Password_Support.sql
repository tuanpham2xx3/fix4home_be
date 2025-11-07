-- Add temp_password column to activation_tokens table
ALTER TABLE activation_tokens 
ADD COLUMN temp_password VARCHAR(500) NULL COMMENT 'Encrypted temporary password for password reset';

-- Add must_change_password flag to users table
ALTER TABLE users 
ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Flag to force password change after temp password reset';

-- Add index for better performance on must_change_password queries
CREATE INDEX idx_users_must_change_password ON users(must_change_password);

-- Add comments for documentation
ALTER TABLE activation_tokens COMMENT = 'Stores activation tokens for email verification, password reset with temp passwords, and account activation. Tokens expire after 30 minutes and can be resent up to 3 times with 60-second cooldown.';
ALTER TABLE users COMMENT = 'Main users table with support for temporary password reset flow requiring password change.';
