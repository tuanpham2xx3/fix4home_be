-- V023__Create_Bookings_Table.sql
-- Migration script for Booking/Order Management System
-- Author: Fix4Home Development Team

-- ================================================================
-- CREATE BOOKINGS TABLE
-- ================================================================
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    address TEXT NOT NULL,
    date DATETIME NOT NULL,
    notes TEXT,
    phone VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    ward_code VARCHAR(20),
    needs_survey BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_bookings_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_bookings_user (user_id),
    INDEX idx_bookings_status (status),
    INDEX idx_bookings_date (date),
    INDEX idx_bookings_created_at (created_at)
);

-- Add constraint for ENUM values
ALTER TABLE bookings 
ADD CONSTRAINT chk_bookings_status 
    CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED'));

