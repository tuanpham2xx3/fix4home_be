-- V001__Create_Base_Tables.sql
-- Migration script for creating base tables
-- Author: Fix4Home Development Team

-- ================================================================
-- 1. CREATE USERS TABLE
-- ================================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    role ENUM('CUSTOMER', 'TECHNICIAN', 'ADMIN') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'PENDING_APPROVAL', 'REJECTED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_users_username (username),
    INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_status (status)
);

-- ================================================================
-- 2. CREATE SERVICES TABLE
-- ================================================================
CREATE TABLE services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    base_price DECIMAL(12,2) DEFAULT 0,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    
    INDEX idx_services_name (name),
    INDEX idx_services_status (status)
);

-- ================================================================
-- 3. CREATE ADDRESSES TABLE
-- ================================================================
CREATE TABLE addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    recipient_name VARCHAR(100),
    recipient_phone VARCHAR(20),
    address_line TEXT,
    ward VARCHAR(100),
    district VARCHAR(100),
    city VARCHAR(100),
    latitude DECIMAL(10,6),
    longitude DECIMAL(10,6),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_addresses_user (user_id),
    INDEX idx_addresses_location (latitude, longitude)
);

-- ================================================================
-- 4. CREATE CUSTOMER_PROFILES TABLE
-- ================================================================
CREATE TABLE customer_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(100),
    date_of_birth DATE,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_customer_profiles_user (user_id)
);

-- ================================================================
-- 5. CREATE TECHNICIAN_PROFILES TABLE
-- ================================================================
CREATE TABLE technician_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(100),
    skills TEXT,
    experience TEXT,
    rating FLOAT DEFAULT 0.0,
    status ENUM('ACTIVE', 'INACTIVE', 'PENDING_APPROVAL', 'REJECTED') NOT NULL DEFAULT 'PENDING_APPROVAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_technician_profiles_user (user_id),
    INDEX idx_technician_profiles_status (status),
    INDEX idx_technician_profiles_rating (rating)
);

-- ================================================================
-- 6. CREATE SKILLS TABLE
-- ================================================================
CREATE TABLE skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    
    INDEX idx_skills_name (name),
    INDEX idx_skills_status (status)
);

-- ================================================================
-- 7. CREATE TECHNICIAN_SKILLS TABLE (Many-to-Many)
-- ================================================================
CREATE TABLE technician_skills (
    technician_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    years_experience INT DEFAULT 0,
    certification_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT') DEFAULT 'BEGINNER',
    
    PRIMARY KEY (technician_id, skill_id),
    FOREIGN KEY (technician_id) REFERENCES technician_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    
    INDEX idx_technician_skills_technician (technician_id),
    INDEX idx_technician_skills_skill (skill_id)
);

-- ================================================================
-- 8. CREATE SERVICE_REQUESTS TABLE
-- ================================================================
CREATE TABLE service_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    technician_id BIGINT,
    address_id BIGINT NOT NULL,
    description TEXT,
    status ENUM('PENDING', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'COMPLAINING') NOT NULL DEFAULT 'PENDING',
    scheduled_time TIMESTAMP,
    completed_time TIMESTAMP,
    price DECIMAL(12,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    FOREIGN KEY (technician_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (address_id) REFERENCES addresses(id) ON DELETE CASCADE,
    
    INDEX idx_service_requests_customer (customer_id),
    INDEX idx_service_requests_service (service_id),
    INDEX idx_service_requests_technician (technician_id),
    INDEX idx_service_requests_status (status),
    INDEX idx_service_requests_created_at (created_at)
);

-- ================================================================
-- 9. CREATE SERVICE_REQUEST_LOGS TABLE
-- ================================================================
CREATE TABLE service_request_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_request_id BIGINT NOT NULL,
    old_status ENUM('PENDING', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'COMPLAINING'),
    new_status ENUM('PENDING', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'COMPLAINING') NOT NULL,
    changed_by BIGINT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_service_request_logs_request (service_request_id),
    INDEX idx_service_request_logs_changed_at (changed_at)
);

-- ================================================================
-- 10. CREATE REFRESH_TOKENS TABLE
-- ================================================================
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_refresh_tokens_user (user_id),
    INDEX idx_refresh_tokens_token (token),
    INDEX idx_refresh_tokens_expiry (expiry_date)
);

-- ================================================================
-- 11. CREATE PAYMENTS TABLE
-- ================================================================
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_request_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_method ENUM('CASH', 'BANK_TRANSFER', 'CREDIT_CARD', 'E_WALLET') NOT NULL,
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(255),
    notes TEXT,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE CASCADE,
    INDEX idx_payments_service_request (service_request_id),
    INDEX idx_payments_status (status),
    INDEX idx_payments_transaction_id (transaction_id)
);

-- ================================================================
-- 12. CREATE FEEDBACKS TABLE
-- ================================================================
CREATE TABLE feedbacks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_request_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE CASCADE,
    INDEX idx_feedbacks_service_request (service_request_id),
    INDEX idx_feedbacks_rating (rating),
    INDEX idx_feedbacks_created_at (created_at)
);

-- ================================================================
-- 13. CREATE NOTIFICATIONS TABLE
-- ================================================================
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('INFO', 'WARNING', 'SUCCESS', 'ERROR') NOT NULL DEFAULT 'INFO',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notifications_user (user_id),
    INDEX idx_notifications_is_read (is_read),
    INDEX idx_notifications_created_at (created_at)
);

-- ================================================================
-- 14. INSERT SAMPLE DATA
-- ================================================================

-- Insert default services
INSERT INTO services (name, description, base_price) VALUES 
('Sửa chữa điện', 'Sửa chữa hệ thống điện trong nhà', 200000),
('Sửa chữa nước', 'Sửa chữa hệ thống nước, ống nước', 150000),
('Sửa chữa điều hòa', 'Sửa chữa và bảo trì điều hòa không khí', 300000),
('Sửa chữa tủ lạnh', 'Sửa chữa và bảo trì tủ lạnh', 250000),
('Sửa chữa máy giặt', 'Sửa chữa và bảo trì máy giặt', 200000);

-- Insert default skills
INSERT INTO skills (name, description) VALUES 
('Điện dân dụng', 'Kiến thức về hệ thống điện dân dụng'),
('Nước và ống nước', 'Kiến thức về hệ thống nước và ống dẫn'),
('Điều hòa không khí', 'Sửa chữa và bảo trì điều hòa'),
('Điện lạnh', 'Kiến thức về hệ thống điện lạnh'),
('Cơ khí', 'Kiến thức cơ khí cơ bản');

-- Create migration log table for tracking
CREATE TABLE migration_log (
    version VARCHAR(50) PRIMARY KEY,
    description TEXT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Log this migration
INSERT INTO migration_log (version, description) 
VALUES ('V001', 'Create base tables - users, services, addresses, profiles, service_requests, etc.'); 