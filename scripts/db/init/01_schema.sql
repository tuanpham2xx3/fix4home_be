-- ================================================================
-- FIX4HOME DATABASE COMPLETE SCHEMA
-- Compatible with MySQL 8.0
-- Auto-executed by MySQL init (docker-entrypoint-initdb.d)
-- ================================================================

-- CREATE DATABASE IF NOT EXISTS fix4home_db;
-- USE fix4home_db;

-- ================================================================
-- 1. BASE TABLES
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

CREATE TABLE services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    base_price DECIMAL(12,2) DEFAULT 0,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    
    INDEX idx_services_name (name),
    INDEX idx_services_status (status)
);

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
    
    is_online BOOLEAN NOT NULL DEFAULT FALSE,
    last_seen_at TIMESTAMP NULL,
    current_latitude DOUBLE PRECISION NULL,
    current_longitude DOUBLE PRECISION NULL,
    current_address VARCHAR(500) NULL,
    working_radius INTEGER NOT NULL DEFAULT 10,
    
    verification_documents VARCHAR(1000) NULL,
    rejection_reason VARCHAR(500) NULL,
    approved_at TIMESTAMP NULL,
    approved_by BIGINT NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_technician_profiles_user (user_id),
    INDEX idx_technician_profiles_status (status),
    INDEX idx_technician_profiles_rating (rating),
    INDEX idx_technician_profiles_online_status (is_online),
    INDEX idx_technician_profiles_location (current_latitude, current_longitude),
    INDEX idx_technician_profiles_last_seen (last_seen_at),
    INDEX idx_technician_profiles_approved_by (approved_by),
    INDEX idx_technician_profiles_approved_at (approved_at)
);

CREATE TABLE skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    
    INDEX idx_skills_name (name),
    INDEX idx_skills_status (status)
);

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

-- 2. SERVICE POSTS SYSTEM (V006)
CREATE TABLE service_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    address_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    estimated_budget DECIMAL(12,2),
    preferred_time DATETIME,
    type VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    max_technicians INT DEFAULT 5,
    expires_at DATETIME,
    selected_technician_id BIGINT,
    selected_at DATETIME,
    final_price DECIMAL(12,2),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_service_posts_customer 
        FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_service_posts_service 
        FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    CONSTRAINT fk_service_posts_address 
        FOREIGN KEY (address_id) REFERENCES addresses(id) ON DELETE CASCADE,
    CONSTRAINT fk_service_posts_selected_technician 
        FOREIGN KEY (selected_technician_id) REFERENCES users(id) ON DELETE SET NULL,
        
    INDEX idx_service_posts_customer (customer_id),
    INDEX idx_service_posts_service (service_id),
    INDEX idx_service_posts_status (status),
    INDEX idx_service_posts_type (type),
    INDEX idx_service_posts_created_at (created_at),
    INDEX idx_service_posts_expires_at (expires_at),
    INDEX idx_service_posts_selected_technician (selected_technician_id),
    
    CONSTRAINT chk_service_posts_type 
        CHECK (type IN ('URGENT', 'CONSULTATION', 'SCHEDULED', 'QUOTATION')),
    CONSTRAINT chk_service_posts_status 
        CHECK (status IN ('DRAFT', 'POSTED', 'RESPONSES_RECEIVED', 'TECHNICIAN_SELECTED', 
                         'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'EXPIRED')),
    CONSTRAINT chk_service_posts_estimated_budget 
        CHECK (estimated_budget IS NULL OR estimated_budget >= 0),
    CONSTRAINT chk_service_posts_final_price 
        CHECK (final_price IS NULL OR final_price >= 0),
    CONSTRAINT chk_service_posts_max_technicians 
        CHECK (max_technicians > 0 AND max_technicians <= 50)
);

CREATE TABLE service_post_responses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_post_id BIGINT NOT NULL,
    technician_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    quoted_price DECIMAL(12,2) NOT NULL,
    estimated_duration INT,
    proposed_time DATETIME,
    is_selected BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_service_post_responses_post 
        FOREIGN KEY (service_post_id) REFERENCES service_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_service_post_responses_technician 
        FOREIGN KEY (technician_id) REFERENCES users(id) ON DELETE CASCADE,
        
    UNIQUE KEY unique_response_per_technician (service_post_id, technician_id),
    
    INDEX idx_service_post_responses_post (service_post_id),
    INDEX idx_service_post_responses_technician (technician_id),
    INDEX idx_service_post_responses_created_at (created_at),
    INDEX idx_service_post_responses_is_selected (is_selected),
    
    CONSTRAINT chk_service_post_responses_quoted_price 
        CHECK (quoted_price >= 0),
    CONSTRAINT chk_service_post_responses_estimated_duration 
        CHECK (estimated_duration IS NULL OR estimated_duration > 0),
    CONSTRAINT chk_service_post_responses_message_not_empty 
        CHECK (CHAR_LENGTH(TRIM(message)) > 0)
);

-- 3. CONSULTATIONS SYSTEM (V007)
CREATE TABLE consultations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_post_id BIGINT NOT NULL,
    technician_id BIGINT NOT NULL,
    proposal TEXT NOT NULL,
    quoted_price DECIMAL(12,2) NOT NULL,
    notes TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at DATETIME,
    
    CONSTRAINT fk_consultations_service_post 
        FOREIGN KEY (service_post_id) REFERENCES service_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_consultations_technician 
        FOREIGN KEY (technician_id) REFERENCES users(id) ON DELETE CASCADE,
        
    UNIQUE KEY unique_consultation_per_technician (service_post_id, technician_id),
    
    INDEX idx_consultations_service_post (service_post_id),
    INDEX idx_consultations_technician (technician_id),
    INDEX idx_consultations_status (status),
    INDEX idx_consultations_submitted_at (submitted_at),
    INDEX idx_consultations_responded_at (responded_at),
    INDEX idx_consultations_quoted_price (quoted_price),
    
    CONSTRAINT chk_consultations_status 
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    CONSTRAINT chk_consultations_quoted_price 
        CHECK (quoted_price >= 0),
    CONSTRAINT chk_consultations_proposal_not_empty 
        CHECK (CHAR_LENGTH(TRIM(proposal)) > 0)
);

-- 4. COMPLAINTS SYSTEM (V008)
CREATE TABLE complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_request_id BIGINT NOT NULL,
    complainant_id BIGINT NOT NULL,
    accused_id BIGINT NOT NULL,
    reason VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    admin_response TEXT,
    resolved_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME,
    
    CONSTRAINT fk_complaints_service_request 
        FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_complaints_complainant 
        FOREIGN KEY (complainant_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_complaints_accused 
        FOREIGN KEY (accused_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_complaints_resolved_by 
        FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL,
        
    CONSTRAINT chk_complaints_different_users 
        CHECK (complainant_id != accused_id),
    
    UNIQUE KEY unique_complaint_per_service_request (service_request_id, complainant_id),
    
    INDEX idx_complaints_service_request (service_request_id),
    INDEX idx_complaints_complainant (complainant_id),
    INDEX idx_complaints_accused (accused_id),
    INDEX idx_complaints_resolved_by (resolved_by),
    INDEX idx_complaints_status (status),
    INDEX idx_complaints_created_at (created_at),
    INDEX idx_complaints_resolved_at (resolved_at),
    INDEX idx_complaints_service_request_status (service_request_id, status),
    INDEX idx_complaints_complainant_status (complainant_id, status),
    INDEX idx_complaints_accused_status (accused_id, status),
    INDEX idx_complaints_status_created_at (status, created_at),
    INDEX idx_complaints_resolved_by_resolved_at (resolved_by, resolved_at),
    
    CONSTRAINT chk_complaints_status 
        CHECK (status IN ('PENDING', 'INVESTIGATING', 'RESOLVED', 'REJECTED')),
    CONSTRAINT chk_complaints_reason_length 
        CHECK (CHAR_LENGTH(TRIM(reason)) BETWEEN 10 AND 200),
    CONSTRAINT chk_complaints_description_length 
        CHECK (CHAR_LENGTH(TRIM(description)) BETWEEN 20 AND 2000),
    CONSTRAINT chk_complaints_reason_not_empty 
        CHECK (CHAR_LENGTH(TRIM(reason)) > 0),
    CONSTRAINT chk_complaints_description_not_empty 
        CHECK (CHAR_LENGTH(TRIM(description)) > 0)
);

-- 5. CHAT SYSTEM (V011)
CREATE TABLE conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    service_request_id BIGINT NULL,
    service_post_id BIGINT NULL,
    consultation_id BIGINT NULL,
    
    customer_id BIGINT NOT NULL,
    technician_id BIGINT NOT NULL,
    
    status ENUM('ACTIVE', 'ARCHIVED', 'BLOCKED') NOT NULL DEFAULT 'ACTIVE',
    last_message_at TIMESTAMP NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE SET NULL,
    FOREIGN KEY (service_post_id) REFERENCES service_posts(id) ON DELETE SET NULL,
    FOREIGN KEY (consultation_id) REFERENCES consultations(id) ON DELETE SET NULL,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (technician_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_conversations_customer (customer_id),
    INDEX idx_conversations_technician (technician_id),
    INDEX idx_conversations_participants (customer_id, technician_id),
    INDEX idx_conversations_service_request (service_request_id),
    INDEX idx_conversations_service_post (service_post_id),
    INDEX idx_conversations_consultation (consultation_id),
    INDEX idx_conversations_status (status),
    INDEX idx_conversations_last_message (last_message_at),
    INDEX idx_conversations_created_at (created_at),
    
    CONSTRAINT chk_different_participants 
        CHECK (customer_id != technician_id)
);

CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NULL,
    
    content TEXT NOT NULL,
    message_type ENUM('TEXT', 'IMAGE', 'LOCATION', 'SYSTEM', 'QUOTATION', 'FILE') NOT NULL DEFAULT 'TEXT',
    attachment_url VARCHAR(500) NULL,
    metadata JSON NULL,
    
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_messages_conversation (conversation_id),
    INDEX idx_messages_sender (sender_id),
    INDEX idx_messages_conversation_time (conversation_id, sent_at DESC),
    INDEX idx_messages_sent_at (sent_at),
    INDEX idx_messages_unread (conversation_id, is_read),
    INDEX idx_messages_type (message_type),
    INDEX idx_messages_unread_user (conversation_id, sender_id, is_read)
);

-- 6. SUPPORTING TABLES
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

-- Default services and skills (same as docs schema)
INSERT INTO services (name, description, base_price) VALUES 
('Sửa chữa điện', 'Sửa chữa hệ thống điện trong nhà', 200000),
('Sửa chữa nước', 'Sửa chữa hệ thống nước, ống nước', 150000),
('Sửa chữa điều hòa', 'Sửa chữa và bảo trì điều hòa không khí', 300000),
('Sửa chữa tủ lạnh', 'Sửa chữa và bảo trì tủ lạnh', 250000),
('Sửa chữa máy giặt', 'Sửa chữa và bảo trì máy giặt', 200000);

INSERT INTO skills (name, description) VALUES 
('Điện dân dụng', 'Kiến thức về hệ thống điện dân dụng'),
('Nước và ống nước', 'Kiến thức về hệ thống nước và ống dẫn'),
('Điều hòa không khí', 'Sửa chữa và bảo trì điều hòa'),
('Điện lạnh', 'Kiến thức về hệ thống điện lạnh'),
('Cơ khí', 'Kiến thức cơ khí cơ bản');

-- ================================================================
-- SCHEMA CREATION COMPLETE
-- ================================================================


