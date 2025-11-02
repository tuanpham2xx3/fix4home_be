-- ================================================================
-- PUSH NOTIFICATION SYSTEM MIGRATION
-- Version: V016
-- Description: Create tables for mobile push notifications
-- Author: Fix4Home Development Team
-- Date: 2024-12-19
-- ================================================================

-- Create device_tokens table
CREATE TABLE device_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'User who owns the device',
    token VARCHAR(512) NOT NULL UNIQUE COMMENT 'Device push notification token',
    platform VARCHAR(20) NOT NULL COMMENT 'Device platform (ANDROID, IOS, WEB)',
    device_id VARCHAR(200) COMMENT 'Unique device identifier',
    device_name VARCHAR(100) COMMENT 'User-friendly device name',
    app_version VARCHAR(20) COMMENT 'App version',
    os_version VARCHAR(50) COMMENT 'Operating system version',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Whether token is active',
    notifications_enabled BOOLEAN DEFAULT TRUE COMMENT 'Whether notifications are enabled',
    last_used_at TIMESTAMP COMMENT 'Last time token was used',
    failed_attempts INT DEFAULT 0 COMMENT 'Number of failed delivery attempts',
    last_failure_at TIMESTAMP COMMENT 'Last failure timestamp',
    user_agent VARCHAR(500) COMMENT 'Browser user agent for web devices',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Registration timestamp',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    PRIMARY KEY (id),
    
    -- Foreign key constraints
    CONSTRAINT fk_device_tokens_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE,
    
    -- Check constraints
    CONSTRAINT chk_device_platform_enum 
        CHECK (platform IN ('ANDROID', 'IOS', 'WEB', 'UNKNOWN')),
    CONSTRAINT chk_failed_attempts_non_negative 
        CHECK (failed_attempts >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Device tokens for push notifications';

-- Create push_notifications table
CREATE TABLE push_notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'Target user for notification',
    device_token_id BIGINT COMMENT 'Specific device token (optional)',
    type VARCHAR(50) NOT NULL COMMENT 'Notification type',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Notification status',
    title VARCHAR(200) NOT NULL COMMENT 'Notification title',
    message TEXT NOT NULL COMMENT 'Notification message',
    data_payload TEXT COMMENT 'JSON data payload',
    image_url VARCHAR(500) COMMENT 'Notification image URL',
    action_url VARCHAR(500) COMMENT 'Action URL when clicked',
    badge_count INT COMMENT 'Badge count for iOS',
    sound VARCHAR(50) DEFAULT 'default' COMMENT 'Sound to play',
    priority VARCHAR(20) DEFAULT 'normal' COMMENT 'Notification priority',
    time_to_live INT DEFAULT 86400 COMMENT 'TTL in seconds',
    scheduled_at TIMESTAMP COMMENT 'Scheduled delivery time',
    sent_at TIMESTAMP COMMENT 'Actual send time',
    delivered_at TIMESTAMP COMMENT 'Delivery confirmation time',
    clicked_at TIMESTAMP COMMENT 'User click time',
    failed_at TIMESTAMP COMMENT 'Failure time',
    failure_reason VARCHAR(500) COMMENT 'Failure reason',
    retry_count INT DEFAULT 0 COMMENT 'Number of retry attempts',
    max_retries INT DEFAULT 3 COMMENT 'Maximum retry attempts',
    external_id VARCHAR(200) COMMENT 'External service message ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    PRIMARY KEY (id),
    
    -- Foreign key constraints
    CONSTRAINT fk_push_notifications_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_push_notifications_device_token 
        FOREIGN KEY (device_token_id) REFERENCES device_tokens(id) 
        ON DELETE SET NULL,
    
    -- Check constraints
    CONSTRAINT chk_notification_status_enum 
        CHECK (status IN ('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'CANCELLED', 'EXPIRED')),
    CONSTRAINT chk_notification_priority_enum 
        CHECK (priority IN ('normal', 'high')),
    CONSTRAINT chk_retry_counts 
        CHECK (retry_count <= max_retries),
    CONSTRAINT chk_time_to_live_positive 
        CHECK (time_to_live IS NULL OR time_to_live > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Push notification records and delivery tracking';

-- ================================================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- ================================================================

-- Device Tokens Indexes
CREATE INDEX idx_device_tokens_user_id 
    ON device_tokens(user_id);

CREATE INDEX idx_device_tokens_token 
    ON device_tokens(token);

CREATE INDEX idx_device_tokens_platform 
    ON device_tokens(platform);

CREATE INDEX idx_device_tokens_is_active 
    ON device_tokens(is_active);

CREATE INDEX idx_device_tokens_last_used 
    ON device_tokens(last_used_at);

-- Composite index for active tokens by user and platform
CREATE INDEX idx_device_tokens_user_platform_active 
    ON device_tokens(user_id, platform, is_active, notifications_enabled);

-- Push Notifications Indexes
CREATE INDEX idx_push_notifications_user_id 
    ON push_notifications(user_id);

CREATE INDEX idx_push_notifications_device_token_id 
    ON push_notifications(device_token_id);

CREATE INDEX idx_push_notifications_status 
    ON push_notifications(status);

CREATE INDEX idx_push_notifications_type 
    ON push_notifications(type);

CREATE INDEX idx_push_notifications_scheduled_at 
    ON push_notifications(scheduled_at);

CREATE INDEX idx_push_notifications_sent_at 
    ON push_notifications(sent_at);

-- Composite index for pending notifications processing
CREATE INDEX idx_push_notifications_pending_processing 
    ON push_notifications(status, scheduled_at, created_at);

-- Index for notification analytics
CREATE INDEX idx_push_notifications_analytics 
    ON push_notifications(type, status, created_at);

-- Index for user notification history
CREATE INDEX idx_push_notifications_user_history 
    ON push_notifications(user_id, created_at);

-- Index for retry processing
CREATE INDEX idx_push_notifications_retry 
    ON push_notifications(status, retry_count, max_retries, failed_at);

-- ================================================================
-- SAMPLE DATA FOR TESTING (Optional - commented out for production)
-- ================================================================

/*
-- Insert sample device tokens
INSERT INTO device_tokens (
    user_id, token, platform, device_id, device_name, app_version, os_version, 
    is_active, notifications_enabled
) VALUES 
-- Android device
(1, 'fGH1jK2lM3nO4pQ5rS6tU7vW8xY9zA0bC1dE2fG3hI4jK5lM6nO7pQ8rS9tU0vW1x', 
 'ANDROID', 'android_device_001', 'Samsung Galaxy S21', '1.0.0', 'Android 12', true, true),

-- iOS device
(1, 'aB2cD3eF4gH5iJ6kL7mN8oP9qR0sT1uV2wX3yZ4aB5cD6eF7gH8iJ9kL0mN1oP2qR', 
 'IOS', 'ios_device_001', 'iPhone 13 Pro', '1.0.0', 'iOS 15.6', true, true),

-- Web device
(2, 'pQ3rS4tU5vW6xY7zA8bC9dE0fG1hI2jK3lM4nO5pQ6rS7tU8vW9xY0zA1bC2dE3fG', 
 'WEB', 'web_device_001', 'Chrome Browser', '1.0.0', 'Chrome 98', true, true);

-- Insert sample push notifications
INSERT INTO push_notifications (
    user_id, device_token_id, type, title, message, data_payload, priority, status
) VALUES 
-- Service request notification
(1, 1, 'SERVICE_REQUEST_ACCEPTED', 'Service Request Accepted', 
 'Your electrical repair request has been accepted by a technician.',
 '{"serviceRequestId": 1, "technicianId": 2}', 'high', 'SENT'),

-- Payment notification
(1, 2, 'PAYMENT_RECEIVED', 'Payment Received', 
 'Payment of $50 has been successfully processed.',
 '{"paymentId": 1, "amount": 50.00}', 'normal', 'DELIVERED'),

-- New message notification
(2, 3, 'NEW_MESSAGE', 'New Message', 
 'You have a new message from John regarding your service request.',
 '{"conversationId": 1, "senderId": 1}', 'normal', 'PENDING');
*/

-- ================================================================
-- VERIFICATION QUERIES
-- ================================================================

-- Verify table creation
SELECT 'device_tokens table created successfully' as status;
SELECT 'push_notifications table created successfully' as status;

-- Show table structures
-- DESCRIBE device_tokens;
-- DESCRIBE push_notifications;

-- Show indexes
-- SHOW INDEX FROM device_tokens;
-- SHOW INDEX FROM push_notifications;

-- ================================================================
-- MAINTENANCE PROCEDURES
-- ================================================================

/*
-- Procedure to clean up old device tokens (inactive for 3 months)
DELIMITER //
CREATE PROCEDURE CleanupStaleDeviceTokens()
BEGIN
    UPDATE device_tokens 
    SET is_active = FALSE 
    WHERE (last_used_at < DATE_SUB(NOW(), INTERVAL 3 MONTH) 
           OR last_used_at IS NULL AND created_at < DATE_SUB(NOW(), INTERVAL 3 MONTH))
    AND is_active = TRUE;
    
    SELECT ROW_COUNT() as updated_tokens;
END //
DELIMITER ;

-- Procedure to clean up old notifications (older than 1 month)
DELIMITER //
CREATE PROCEDURE CleanupOldNotifications()
BEGIN
    DELETE FROM push_notifications 
    WHERE created_at < DATE_SUB(NOW(), INTERVAL 1 MONTH)
    AND status IN ('DELIVERED', 'FAILED', 'EXPIRED', 'CANCELLED');
    
    SELECT ROW_COUNT() as deleted_notifications;
END //
DELIMITER ;

-- Procedure to mark expired notifications
DELIMITER //
CREATE PROCEDURE MarkExpiredNotifications()
BEGIN
    UPDATE push_notifications 
    SET status = 'EXPIRED'
    WHERE status = 'PENDING' 
    AND time_to_live IS NOT NULL
    AND created_at <= DATE_SUB(NOW(), INTERVAL time_to_live SECOND);
    
    SELECT ROW_COUNT() as expired_notifications;
END //
DELIMITER ;
*/

-- ================================================================
-- COMMENTS AND DOCUMENTATION
-- ================================================================

/*
PUSH NOTIFICATION SYSTEM FEATURES:

1. DEVICE TOKEN MANAGEMENT
   - Multi-platform support (Android, iOS, Web)
   - Token lifecycle management
   - Automatic cleanup of stale tokens
   - Failed delivery tracking

2. NOTIFICATION DELIVERY
   - Immediate and scheduled delivery
   - Retry mechanism for failed deliveries
   - TTL (Time To Live) support
   - Priority-based delivery

3. NOTIFICATION TYPES
   - 25+ predefined notification types
   - Service request lifecycle notifications
   - Payment and transaction notifications
   - Chat and messaging notifications
   - System and promotional notifications

4. DELIVERY TRACKING
   - Send confirmation tracking
   - Delivery confirmation tracking
   - Click-through tracking
   - Failure reason logging

5. ANALYTICS SUPPORT
   - Delivery rate statistics
   - User engagement metrics
   - Platform performance analysis
   - Notification type effectiveness

6. SCALABILITY FEATURES
   - Batch notification support
   - Background processing ready
   - Efficient indexing for performance
   - Automatic cleanup mechanisms

INTEGRATION POINTS:
- FCM (Firebase Cloud Messaging) for Android/Web
- APNs (Apple Push Notification Service) for iOS
- WebSocket fallback for real-time notifications
- Email notification fallback option

SECURITY CONSIDERATIONS:
- User permission management
- Device token encryption
- Payload size limits
- Rate limiting protection
*/

