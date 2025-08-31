-- ================================================================
-- V017: CREATE AUDIT LOGS TABLE
-- Security and audit logging system
-- ================================================================

-- Audit logs table for request tracking and security monitoring
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(200),
    resource_id BIGINT,
    method VARCHAR(10),
    endpoint VARCHAR(500),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    request_body TEXT,
    response_status INT,
    processing_time BIGINT,
    session_id VARCHAR(100),
    success BOOLEAN,
    error_message VARCHAR(1000),
    additional_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_audit_logs_user_id (user_id),
    INDEX idx_audit_logs_action (action),
    INDEX idx_audit_logs_resource (resource),
    INDEX idx_audit_logs_created_at (created_at),
    INDEX idx_audit_logs_ip_address (ip_address),
    INDEX idx_audit_logs_endpoint (endpoint),
    INDEX idx_audit_logs_success (success),
    INDEX idx_audit_logs_response_status (response_status)
);

-- Create partition for better performance (MySQL 8.0+)
-- ALTER TABLE audit_logs PARTITION BY RANGE (YEAR(created_at)) (
--     PARTITION p2024 VALUES LESS THAN (2025),
--     PARTITION p2025 VALUES LESS THAN (2026),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- Add comments for documentation
ALTER TABLE audit_logs COMMENT = 'Audit logs for security monitoring and request tracking';
ALTER TABLE audit_logs MODIFY COLUMN user_id BIGINT COMMENT 'ID of the user who made the request (null for anonymous)';
ALTER TABLE audit_logs MODIFY COLUMN action VARCHAR(100) COMMENT 'Action performed (CREATE, UPDATE, DELETE, VIEW, etc.)';
ALTER TABLE audit_logs MODIFY COLUMN resource VARCHAR(200) COMMENT 'Resource type being accessed (USER, SERVICE, etc.)';
ALTER TABLE audit_logs MODIFY COLUMN ip_address VARCHAR(45) COMMENT 'Client IP address (supports IPv6)';
ALTER TABLE audit_logs MODIFY COLUMN processing_time BIGINT COMMENT 'Request processing time in milliseconds';
ALTER TABLE audit_logs MODIFY COLUMN success BOOLEAN COMMENT 'Whether the request was successful';
