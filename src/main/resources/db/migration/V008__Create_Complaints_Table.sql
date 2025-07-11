-- V008__Create_Complaints_Table.sql
-- Migration script for Complaint System

-- Create complaints table
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
    
    -- Foreign key constraints
    CONSTRAINT fk_complaints_service_request 
        FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_complaints_complainant 
        FOREIGN KEY (complainant_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_complaints_accused 
        FOREIGN KEY (accused_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_complaints_resolved_by 
        FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL,
        
    -- Business constraints
    -- Ensure complainant and accused are different users
    CONSTRAINT chk_complaints_different_users 
        CHECK (complainant_id != accused_id),
    
    -- Ensure one complaint per complainant per service request
    UNIQUE KEY unique_complaint_per_service_request (service_request_id, complainant_id),
    
    -- Basic indexes for foreign keys
    INDEX idx_complaints_service_request (service_request_id),
    INDEX idx_complaints_complainant (complainant_id),
    INDEX idx_complaints_accused (accused_id),
    INDEX idx_complaints_resolved_by (resolved_by),
    INDEX idx_complaints_status (status),
    INDEX idx_complaints_created_at (created_at),
    INDEX idx_complaints_resolved_at (resolved_at)
);

-- Add constraints for ENUM values
ALTER TABLE complaints 
ADD CONSTRAINT chk_complaints_status 
    CHECK (status IN ('PENDING', 'INVESTIGATING', 'RESOLVED', 'REJECTED'));

-- Add constraints for string length validation
ALTER TABLE complaints 
ADD CONSTRAINT chk_complaints_reason_length 
    CHECK (CHAR_LENGTH(TRIM(reason)) BETWEEN 10 AND 200);

ALTER TABLE complaints 
ADD CONSTRAINT chk_complaints_description_length 
    CHECK (CHAR_LENGTH(TRIM(description)) BETWEEN 20 AND 2000);

-- Add constraint to ensure reason and description are not empty
ALTER TABLE complaints 
ADD CONSTRAINT chk_complaints_reason_not_empty 
    CHECK (CHAR_LENGTH(TRIM(reason)) > 0);

ALTER TABLE complaints 
ADD CONSTRAINT chk_complaints_description_not_empty 
    CHECK (CHAR_LENGTH(TRIM(description)) > 0);

-- Add constraint to ensure resolved_at is set when status is RESOLVED or REJECTED
-- Note: This constraint is commented out as it's difficult to enforce at DB level
-- We'll handle this validation in the application layer instead
-- ALTER TABLE complaints 
-- ADD CONSTRAINT chk_complaints_resolved_at_when_resolved 
--     CHECK (
--         (status IN ('PENDING', 'INVESTIGATING') AND resolved_at IS NULL) OR 
--         (status IN ('RESOLVED', 'REJECTED') AND resolved_at IS NOT NULL)
--     );

-- Add constraint to ensure admin_response is provided when complaint is resolved/rejected
-- ALTER TABLE complaints 
-- ADD CONSTRAINT chk_complaints_admin_response_when_resolved 
--     CHECK (
--         (status IN ('PENDING', 'INVESTIGATING')) OR 
--         (status IN ('RESOLVED', 'REJECTED') AND admin_response IS NOT NULL AND CHAR_LENGTH(TRIM(admin_response)) > 0)
--     );

-- Add constraint to ensure resolved_by is set when status is not PENDING
-- ALTER TABLE complaints 
-- ADD CONSTRAINT chk_complaints_resolved_by_when_not_pending 
--     CHECK (
--         (status = 'PENDING' AND resolved_by IS NULL) OR 
--         (status IN ('INVESTIGATING', 'RESOLVED', 'REJECTED') AND resolved_by IS NOT NULL)
--     );

-- Create composite indexes for better query performance
CREATE INDEX idx_complaints_service_request_status ON complaints(service_request_id, status);
CREATE INDEX idx_complaints_complainant_status ON complaints(complainant_id, status);
CREATE INDEX idx_complaints_accused_status ON complaints(accused_id, status);
CREATE INDEX idx_complaints_status_created_at ON complaints(status, created_at);
CREATE INDEX idx_complaints_resolved_by_resolved_at ON complaints(resolved_by, resolved_at);

-- Create index for admin operations (MySQL doesn't support partial indexes with WHERE clause)
CREATE INDEX idx_complaints_pending_by_created_at ON complaints(status, created_at);

-- Sample data for testing (optional)
-- Note: This assumes existing service requests and users in the system
-- These would typically be inserted by the application in real use

-- INSERT INTO complaints (service_request_id, complainant_id, accused_id, reason, description, status)
-- VALUES 
-- (1, 2, 4, 'Thợ không đến đúng giờ hẹn', 'Thợ hẹn 8h sáng nhưng đến 10h mới tới. Tôi phải xin nghỉ làm để chờ mà thợ không thông báo trước. Điều này ảnh hưởng đến công việc của tôi.', 'PENDING'),
-- (2, 2, 5, 'Chất lượng công việc không đạt yêu cầu', 'Sau khi sửa xong, máy lạnh vẫn không mát. Thợ nói đã sửa xong nhưng thực tế vấn đề vẫn còn. Tôi yêu cầu kiểm tra lại nhưng thợ không phản hồi.', 'INVESTIGATING'),
-- (3, 6, 2, 'Khách hàng không thanh toán đủ', 'Khách hàng chỉ thanh toán 70% số tiền đã thỏa thuận với lý do không hài lòng, nhưng tôi đã hoàn thành đúng yêu cầu. Khách hàng không đưa ra lý do cụ thể.', 'PENDING'); 