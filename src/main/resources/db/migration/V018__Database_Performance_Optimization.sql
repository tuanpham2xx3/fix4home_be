-- ================================================================
-- V018: DATABASE PERFORMANCE OPTIMIZATION
-- Add missing indexes, optimize existing ones, and improve performance
-- Author: Fix4Home Development Team
-- Date: 2024-12-19
-- ================================================================

-- ================================================================
-- 1. ADD MISSING PERFORMANCE INDEXES
-- ================================================================

-- Improve service_requests query performance
SET @idx_name := 'idx_service_requests_customer_status';
SET @tbl_name := 'service_requests';
SET @drop_index_sql := NULL;
SELECT CONCAT('ALTER TABLE ', @tbl_name, ' DROP INDEX ', @idx_name) INTO @drop_index_sql
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = @tbl_name AND index_name = @idx_name
LIMIT 1;
SET @drop_index_sql := IF(@drop_index_sql IS NULL, 'SELECT 1', @drop_index_sql);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX idx_service_requests_customer_status 
    ON service_requests(customer_id, status, created_at DESC);

SET @idx_name := 'idx_service_requests_technician_status';
SET @tbl_name := 'service_requests';
SET @drop_index_sql := NULL;
SELECT CONCAT('ALTER TABLE ', @tbl_name, ' DROP INDEX ', @idx_name) INTO @drop_index_sql
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = @tbl_name AND index_name = @idx_name
LIMIT 1;
SET @drop_index_sql := IF(@drop_index_sql IS NULL, 'SELECT 1', @drop_index_sql);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX idx_service_requests_technician_status 
    ON service_requests(technician_id, status, created_at DESC);

SET @idx_name := 'idx_service_requests_status_created';
SET @tbl_name := 'service_requests';
SET @drop_index_sql := NULL;
SELECT CONCAT('ALTER TABLE ', @tbl_name, ' DROP INDEX ', @idx_name) INTO @drop_index_sql
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = @tbl_name AND index_name = @idx_name
LIMIT 1;
SET @drop_index_sql := IF(@drop_index_sql IS NULL, 'SELECT 1', @drop_index_sql);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX idx_service_requests_status_created 
    ON service_requests(status, created_at DESC);

-- Improve technician_profiles search performance
SET @idx_name := 'idx_technician_profiles_rating_status';
SET @tbl_name := 'technician_profiles';
SET @drop_index_sql := NULL;
SELECT CONCAT('ALTER TABLE ', @tbl_name, ' DROP INDEX ', @idx_name) INTO @drop_index_sql
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = @tbl_name AND index_name = @idx_name
LIMIT 1;
SET @drop_index_sql := IF(@drop_index_sql IS NULL, 'SELECT 1', @drop_index_sql);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX idx_technician_profiles_rating_status 
    ON technician_profiles(rating DESC, status);

-- Improve payment queries
SET @idx_name := 'idx_payments_status_created';
SET @tbl_name := 'payments';
SET @drop_index_sql := NULL;
SELECT CONCAT('ALTER TABLE ', @tbl_name, ' DROP INDEX ', @idx_name) INTO @drop_index_sql
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = @tbl_name AND index_name = @idx_name
LIMIT 1;
SET @drop_index_sql := IF(@drop_index_sql IS NULL, 'SELECT 1', @drop_index_sql);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX idx_payments_status_created 
    ON payments(status, created_at DESC);

-- Improve feedback queries for ratings
SET @idx_name := 'idx_feedbacks_rating_created';
SET @tbl_name := 'feedbacks';
SET @drop_index_sql := NULL;
SELECT CONCAT('ALTER TABLE ', @tbl_name, ' DROP INDEX ', @idx_name) INTO @drop_index_sql
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = @tbl_name AND index_name = @idx_name
LIMIT 1;
SET @drop_index_sql := IF(@drop_index_sql IS NULL, 'SELECT 1', @drop_index_sql);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX idx_feedbacks_rating_created 
    ON feedbacks(rating, created_at DESC);

-- Improve notification queries
SET @idx_name := 'idx_notifications_user_read_created';
SET @tbl_name := 'notifications';
SET @drop_index_sql := NULL;
SELECT CONCAT('ALTER TABLE ', @tbl_name, ' DROP INDEX ', @idx_name) INTO @drop_index_sql
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = @tbl_name AND index_name = @idx_name
LIMIT 1;
SET @drop_index_sql := IF(@drop_index_sql IS NULL, 'SELECT 1', @drop_index_sql);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX idx_notifications_user_read_created 
    ON notifications(user_id, is_read, created_at DESC);

-- Improve refresh token cleanup queries
SET @idx_name := 'idx_refresh_tokens_expiry_created';
SET @tbl_name := 'refresh_tokens';
SET @drop_index_sql := NULL;
SELECT CONCAT('ALTER TABLE ', @tbl_name, ' DROP INDEX ', @idx_name) INTO @drop_index_sql
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = @tbl_name AND index_name = @idx_name
LIMIT 1;
SET @drop_index_sql := IF(@drop_index_sql IS NULL, 'SELECT 1', @drop_index_sql);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX idx_refresh_tokens_expiry_created 
    ON refresh_tokens(expiry_date, created_at);

-- ================================================================
-- 2. OPTIMIZE EXISTING TABLES FOR PERFORMANCE
-- ================================================================

-- Add covering indexes for common queries
SET @idx_name := 'idx_users_role_status_email';
SET @tbl_name := 'users';
SET @drop_index_sql := NULL;
SELECT CONCAT('ALTER TABLE ', @tbl_name, ' DROP INDEX ', @idx_name) INTO @drop_index_sql
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = @tbl_name AND index_name = @idx_name
LIMIT 1;
SET @drop_index_sql := IF(@drop_index_sql IS NULL, 'SELECT 1', @drop_index_sql);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX idx_users_role_status_email 
    ON users(role, status, email);

-- Optimize address queries for location services
SET @idx_name := 'idx_addresses_user_city_district';
SET @tbl_name := 'addresses';
SET @drop_index_sql := NULL;
SELECT CONCAT('ALTER TABLE ', @tbl_name, ' DROP INDEX ', @idx_name) INTO @drop_index_sql
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = @tbl_name AND index_name = @idx_name
LIMIT 1;
SET @drop_index_sql := IF(@drop_index_sql IS NULL, 'SELECT 1', @drop_index_sql);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX idx_addresses_user_city_district 
    ON addresses(user_id, city, district);

-- Optimize service queries
SET @idx_name := 'idx_services_status_name';
SET @tbl_name := 'services';
SET @drop_index_sql := NULL;
SELECT CONCAT('ALTER TABLE ', @tbl_name, ' DROP INDEX ', @idx_name) INTO @drop_index_sql
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = @tbl_name AND index_name = @idx_name
LIMIT 1;
SET @drop_index_sql := IF(@drop_index_sql IS NULL, 'SELECT 1', @drop_index_sql);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX idx_services_status_name 
    ON services(status, name);

-- Optimize skills queries for technician matching
SET @idx_name := 'idx_skills_status_name';
SET @tbl_name := 'skills';
SET @drop_index_sql := NULL;
SELECT CONCAT('ALTER TABLE ', @tbl_name, ' DROP INDEX ', @idx_name) INTO @drop_index_sql
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = @tbl_name AND index_name = @idx_name
LIMIT 1;
SET @drop_index_sql := IF(@drop_index_sql IS NULL, 'SELECT 1', @drop_index_sql);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX idx_skills_status_name 
    ON skills(status, name);

-- ================================================================
-- 3. ADD PARTITIONING FOR LARGE TABLES (MySQL 8.0+)
-- ================================================================

-- Note: Partitioning commands are commented out as they require careful planning
-- and should be applied based on actual data volume and usage patterns

/*
-- Partition audit_logs by month for better performance
ALTER TABLE audit_logs PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at)) (
    PARTITION p202412 VALUES LESS THAN (202501),
    PARTITION p202501 VALUES LESS THAN (202502),
    PARTITION p202502 VALUES LESS THAN (202503),
    PARTITION p202503 VALUES LESS THAN (202504),
    PARTITION p202504 VALUES LESS THAN (202505),
    PARTITION p202505 VALUES LESS THAN (202506),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- Partition search_history by quarter for performance
ALTER TABLE search_history PARTITION BY RANGE (YEAR(created_at) * 100 + QUARTER(created_at)) (
    PARTITION p2024q4 VALUES LESS THAN (202501),
    PARTITION p2025q1 VALUES LESS THAN (202502),
    PARTITION p2025q2 VALUES LESS THAN (202503),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
*/

-- ================================================================
-- 4. ADD STORED PROCEDURES FOR COMMON OPERATIONS
-- ================================================================

-- Procedure to get technician rankings
DROP PROCEDURE IF EXISTS GetTechnicianRankings;
DELIMITER //
CREATE PROCEDURE GetTechnicianRankings(
    IN p_limit INT,
    IN p_min_rating FLOAT,
    IN p_service_id BIGINT
)
BEGIN
    DECLARE v_limit INT DEFAULT 10;
    DECLARE v_min_rating FLOAT DEFAULT 0.0;
    DECLARE v_service_id BIGINT DEFAULT NULL;

    IF p_limit IS NOT NULL THEN
        SET v_limit = p_limit;
    END IF;

    IF p_min_rating IS NOT NULL THEN
        SET v_min_rating = p_min_rating;
    END IF;

    IF p_service_id IS NOT NULL THEN
        SET v_service_id = p_service_id;
    END IF;

    SELECT 
        tp.id,
        tp.user_id,
        tp.full_name,
        tp.rating,
        COUNT(DISTINCT sr.id) as completed_jobs,
        AVG(f.rating) as avg_feedback_rating,
        GROUP_CONCAT(DISTINCT s.name SEPARATOR ', ') as skills
    FROM technician_profiles tp
    INNER JOIN users u ON tp.user_id = u.id
    LEFT JOIN service_requests sr ON tp.user_id = sr.technician_id 
        AND sr.status = 'COMPLETED'
    LEFT JOIN feedbacks f ON sr.id = f.service_request_id
    LEFT JOIN technician_skills ts ON tp.id = ts.technician_id
    LEFT JOIN skills s ON ts.skill_id = s.id
    WHERE tp.status = 'ACTIVE'
        AND u.status = 'ACTIVE'
        AND tp.rating >= v_min_rating
        AND (v_service_id IS NULL OR s.id = v_service_id)
    GROUP BY tp.id, tp.user_id, tp.full_name, tp.rating
    ORDER BY tp.rating DESC, completed_jobs DESC
    LIMIT v_limit;
END //
DELIMITER ;

-- Procedure to clean up expired tokens
DROP PROCEDURE IF EXISTS CleanupExpiredTokens;
DELIMITER //
CREATE PROCEDURE CleanupExpiredTokens()
BEGIN
    DECLARE deleted_count INT DEFAULT 0;
    
    DELETE FROM refresh_tokens 
    WHERE expiry_date < NOW();
    
    SET deleted_count = ROW_COUNT();
    
    SELECT CONCAT('Cleaned up ', deleted_count, ' expired tokens') as result;
END //
DELIMITER ;

-- ================================================================
-- 5. ADD VIEWS FOR COMMON QUERIES
-- ================================================================

-- View for active technicians with their skills
DROP VIEW IF EXISTS active_technicians_with_skills;
CREATE VIEW active_technicians_with_skills AS
SELECT 
    tp.id as technician_profile_id,
    tp.user_id,
    u.username,
    u.email,
    u.phone_number,
    tp.full_name,
    tp.rating,
    tp.status as technician_status,
    GROUP_CONCAT(DISTINCT s.name ORDER BY s.name SEPARATOR ', ') as skills,
    COUNT(DISTINCT ts.skill_id) as skill_count,
    tp.created_at,
    tp.updated_at
FROM technician_profiles tp
INNER JOIN users u ON tp.user_id = u.id
LEFT JOIN technician_skills ts ON tp.id = ts.technician_id
LEFT JOIN skills s ON ts.skill_id = s.id AND s.status = 'ACTIVE'
WHERE tp.status = 'ACTIVE' 
    AND u.status = 'ACTIVE'
GROUP BY tp.id, tp.user_id, u.username, u.email, u.phone_number, 
         tp.full_name, tp.rating, tp.status, tp.created_at, tp.updated_at;

-- View for service request summary
DROP VIEW IF EXISTS service_request_summary;
CREATE VIEW service_request_summary AS
SELECT 
    sr.id,
    sr.customer_id,
    c_user.username as customer_username,
    sr.technician_id,
    t_user.username as technician_username,
    sr.service_id,
    s.name as service_name,
    sr.status,
    sr.price,
    sr.scheduled_time,
    sr.completed_time,
    sr.created_at,
    f.rating as customer_rating,
    f.comment as customer_feedback,
    DATEDIFF(sr.completed_time, sr.created_at) as completion_days
FROM service_requests sr
INNER JOIN users c_user ON sr.customer_id = c_user.id
LEFT JOIN users t_user ON sr.technician_id = t_user.id
INNER JOIN services s ON sr.service_id = s.id
LEFT JOIN feedbacks f ON sr.id = f.service_request_id;

-- ================================================================
-- 6. ADD TRIGGERS FOR DATA CONSISTENCY
-- ================================================================

-- Trigger to update conversation last_message_at when new message is added
DROP TRIGGER IF EXISTS tr_update_conversation_last_message;
DELIMITER //
CREATE TRIGGER tr_update_conversation_last_message
    AFTER INSERT ON messages
    FOR EACH ROW
BEGIN
    UPDATE conversations 
    SET last_message_at = NEW.sent_at,
        updated_at = NOW()
    WHERE id = NEW.conversation_id;
END //
DELIMITER ;

-- Trigger to update technician rating when feedback is added/updated
DROP TRIGGER IF EXISTS tr_update_technician_rating_insert;
DELIMITER //
CREATE TRIGGER tr_update_technician_rating_insert
    AFTER INSERT ON feedbacks
    FOR EACH ROW
BEGIN
    UPDATE technician_profiles tp
    INNER JOIN service_requests sr ON tp.user_id = sr.technician_id
    SET tp.rating = (
        SELECT AVG(f.rating)
        FROM feedbacks f
        INNER JOIN service_requests sr2 ON f.service_request_id = sr2.id
        WHERE sr2.technician_id = tp.user_id
    )
    WHERE sr.id = NEW.service_request_id;
END //
DELIMITER ;

DROP TRIGGER IF EXISTS tr_update_technician_rating_update;
DELIMITER //
CREATE TRIGGER tr_update_technician_rating_update
    AFTER UPDATE ON feedbacks
    FOR EACH ROW
BEGIN
    UPDATE technician_profiles tp
    INNER JOIN service_requests sr ON tp.user_id = sr.technician_id
    SET tp.rating = (
        SELECT AVG(f.rating)
        FROM feedbacks f
        INNER JOIN service_requests sr2 ON f.service_request_id = sr2.id
        WHERE sr2.technician_id = tp.user_id
    )
    WHERE sr.id = NEW.service_request_id;
END //
DELIMITER ;

-- ================================================================
-- 7. CREATE PERFORMANCE MONITORING TABLES
-- ================================================================

-- Table to track slow queries
DROP TABLE IF EXISTS slow_query_log;
CREATE TABLE slow_query_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    query_hash VARCHAR(64) NOT NULL,
    query_text TEXT NOT NULL,
    execution_time DECIMAL(10,6) NOT NULL,
    rows_examined BIGINT,
    rows_sent BIGINT,
    user_id BIGINT,
    endpoint VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_slow_query_hash (query_hash),
    INDEX idx_slow_query_time (execution_time DESC),
    INDEX idx_slow_query_created (created_at),
    INDEX idx_slow_query_endpoint (endpoint)
) COMMENT = 'Application-level slow query logging';

-- Table to track database statistics
DROP TABLE IF EXISTS database_stats;
CREATE TABLE database_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15,6) NOT NULL,
    metric_type ENUM('COUNTER', 'GAUGE', 'HISTOGRAM') NOT NULL,
    tags JSON,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_db_stats_name (metric_name),
    INDEX idx_db_stats_recorded (recorded_at),
    INDEX idx_db_stats_name_recorded (metric_name, recorded_at)
) COMMENT = 'Database performance metrics';

-- ================================================================
-- 8. ADD FULL-TEXT SEARCH INDEXES
-- ================================================================

-- Full-text search for service descriptions
SET @idx_name := 'idx_services_fulltext';
SET @tbl_name := 'services';
SET @drop_index_sql := NULL;
SELECT CONCAT('ALTER TABLE ', @tbl_name, ' DROP INDEX ', @idx_name) INTO @drop_index_sql
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = @tbl_name AND index_name = @idx_name
LIMIT 1;
SET @drop_index_sql := IF(@drop_index_sql IS NULL, 'SELECT 1', @drop_index_sql);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE FULLTEXT INDEX idx_services_fulltext ON services (name, description);

-- Full-text search for service request descriptions
SET @idx_name := 'idx_service_requests_fulltext';
SET @tbl_name := 'service_requests';
SET @drop_index_sql := NULL;
SELECT CONCAT('ALTER TABLE ', @tbl_name, ' DROP INDEX ', @idx_name) INTO @drop_index_sql
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = @tbl_name AND index_name = @idx_name
LIMIT 1;
SET @drop_index_sql := IF(@drop_index_sql IS NULL, 'SELECT 1', @drop_index_sql);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE FULLTEXT INDEX idx_service_requests_fulltext ON service_requests (description);

-- Full-text search for technician skills and experience
SET @idx_name := 'idx_technician_profiles_fulltext';
SET @tbl_name := 'technician_profiles';
SET @drop_index_sql := NULL;
SELECT CONCAT('ALTER TABLE ', @tbl_name, ' DROP INDEX ', @idx_name) INTO @drop_index_sql
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name = @tbl_name AND index_name = @idx_name
LIMIT 1;
SET @drop_index_sql := IF(@drop_index_sql IS NULL, 'SELECT 1', @drop_index_sql);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE FULLTEXT INDEX idx_technician_profiles_fulltext ON technician_profiles (skills, experience);

-- ================================================================
-- 9. OPTIMIZATION SETTINGS RECOMMENDATIONS
-- ================================================================

/*
-- MySQL Configuration Recommendations (add to my.cnf):

[mysqld]
# InnoDB Settings for Fix4Home
innodb_buffer_pool_size = 1G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
innodb_file_per_table = 1
innodb_buffer_pool_instances = 8

# Query Cache (MySQL 5.7 and below)
query_cache_type = 1
query_cache_size = 256M

# Connection Settings
max_connections = 200
max_connect_errors = 1000

# Performance Schema
performance_schema = ON
performance_schema_instrument = 'statement/%=ON'

# Slow Query Log
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow-query.log
long_query_time = 2

# Binary Logging
log_bin = mysql-bin
expire_logs_days = 7
max_binlog_size = 100M
*/

-- ================================================================
-- 10. CREATE MAINTENANCE PROCEDURES
-- ================================================================

-- Procedure to analyze table statistics
DROP PROCEDURE IF EXISTS AnalyzeTableStatistics;
DELIMITER //
CREATE PROCEDURE AnalyzeTableStatistics()
BEGIN
    ANALYZE TABLE users, services, addresses, customer_profiles, technician_profiles,
                 skills, technician_skills, service_requests, service_request_logs,
                 refresh_tokens, payments, feedbacks, notifications, conversations,
                 messages, search_history, saved_searches, audit_logs;
                 
    SELECT 'Table analysis completed' as result;
END //
DELIMITER ;

-- Procedure to optimize tables
DROP PROCEDURE IF EXISTS OptimizeTables;
DELIMITER //
CREATE PROCEDURE OptimizeTables()
BEGIN
    OPTIMIZE TABLE service_requests, messages, search_history, audit_logs;
    SELECT 'Table optimization completed' as result;
END //
DELIMITER ;

-- ================================================================
-- 11. MIGRATION COMPLETION
-- ================================================================

-- Update migration log
DELETE FROM migration_log WHERE version = 'V018';
INSERT INTO migration_log (version, description) 
VALUES ('V018', 'Database Performance Optimization - Added indexes, views, triggers, and stored procedures');

-- Performance optimization completion message
SELECT 'Database performance optimization migration V018 completed successfully' as status;

-- Show some statistics
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    AVG_ROW_LENGTH,
    DATA_LENGTH,
    INDEX_LENGTH,
    (DATA_LENGTH + INDEX_LENGTH) as TOTAL_SIZE
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TOTAL_SIZE DESC;






