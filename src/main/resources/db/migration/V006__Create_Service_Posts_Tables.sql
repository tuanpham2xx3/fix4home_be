-- V006__Create_Service_Posts_Tables.sql
-- Migration script for Service Posts System

-- Create service_posts table
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
    INDEX idx_service_posts_selected_technician (selected_technician_id)
);

-- Create service_post_responses table
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
        
    -- Ensure one response per technician per post
    UNIQUE KEY unique_response_per_technician (service_post_id, technician_id),
    
    INDEX idx_service_post_responses_post (service_post_id),
    INDEX idx_service_post_responses_technician (technician_id),
    INDEX idx_service_post_responses_created_at (created_at),
    INDEX idx_service_post_responses_is_selected (is_selected)
);

-- Add constraints for ENUM values
ALTER TABLE service_posts 
ADD CONSTRAINT chk_service_posts_type 
    CHECK (type IN ('URGENT', 'CONSULTATION', 'SCHEDULED', 'QUOTATION'));

ALTER TABLE service_posts 
ADD CONSTRAINT chk_service_posts_status 
    CHECK (status IN ('DRAFT', 'POSTED', 'RESPONSES_RECEIVED', 'TECHNICIAN_SELECTED', 
                     'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'EXPIRED'));

-- Add constraints for positive values
ALTER TABLE service_posts 
ADD CONSTRAINT chk_service_posts_estimated_budget 
    CHECK (estimated_budget IS NULL OR estimated_budget >= 0);

ALTER TABLE service_posts 
ADD CONSTRAINT chk_service_posts_final_price 
    CHECK (final_price IS NULL OR final_price >= 0);

ALTER TABLE service_posts 
ADD CONSTRAINT chk_service_posts_max_technicians 
    CHECK (max_technicians > 0 AND max_technicians <= 50);

ALTER TABLE service_post_responses 
ADD CONSTRAINT chk_service_post_responses_quoted_price 
    CHECK (quoted_price > 0);

ALTER TABLE service_post_responses 
ADD CONSTRAINT chk_service_post_responses_estimated_duration 
    CHECK (estimated_duration IS NULL OR estimated_duration > 0);

-- Insert some sample data for testing (optional)
-- Note: This assumes existing users, services, and addresses in the system

-- Sample service posts (these would be inserted by the application in real use)
-- INSERT INTO service_posts (customer_id, service_id, address_id, title, description, estimated_budget, type, status)
-- VALUES 
-- (1, 1, 1, 'Sửa chữa ổ cắm điện', 'Cần sửa ổ cắm điện trong phòng khách bị chập', 200000, 'URGENT', 'POSTED'),
-- (2, 2, 2, 'Lắp đặt máy lạnh mới', 'Cần lắp đặt máy lạnh 1.5HP cho phòng ngủ', 500000, 'SCHEDULED', 'POSTED'),
-- (3, 3, 3, 'Sửa chữa đường ống nước', 'Đường ống nước bị rò rỉ ở khu vực bếp', 300000, 'CONSULTATION', 'POSTED'); 