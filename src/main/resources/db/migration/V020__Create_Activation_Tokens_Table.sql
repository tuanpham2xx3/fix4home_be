-- Create activation_tokens table for email activation links
CREATE TABLE activation_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(36) NOT NULL UNIQUE COMMENT 'UUID token for activation',
    email VARCHAR(255) NOT NULL COMMENT 'Email address',
    action VARCHAR(50) NOT NULL COMMENT 'Action type: registration, password_reset, email_verification',
    user_id BIGINT NOT NULL COMMENT 'Reference to users table',
    expires_at DATETIME NOT NULL COMMENT 'Token expiration time',
    used BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether token has been used',
    used_at DATETIME NULL COMMENT 'When token was used',
    send_count INT NOT NULL DEFAULT 1 COMMENT 'Number of times email was sent',
    last_sent_at DATETIME NOT NULL COMMENT 'Last time email was sent',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    -- Foreign key constraint
    CONSTRAINT fk_activation_tokens_user_id 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_activation_token (token),
    INDEX idx_activation_email_action (email, action),
    INDEX idx_activation_expires_at (expires_at),
    INDEX idx_activation_user_id (user_id),
    INDEX idx_activation_used (used),
    INDEX idx_activation_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Table for storing email activation tokens';

-- Add some initial comments for documentation
ALTER TABLE activation_tokens COMMENT = 'Stores activation tokens for email verification, password reset, and account activation. Tokens expire after 30 minutes and can be resent up to 3 times with 60-second cooldown.';
