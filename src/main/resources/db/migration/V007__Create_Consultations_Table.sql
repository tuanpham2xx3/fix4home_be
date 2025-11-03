-- V007__Create_Consultations_Table.sql
-- Migration script for Consultation & Quotation System

-- Create consultations table
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
        
    -- Ensure one consultation per technician per service post
    UNIQUE KEY unique_consultation_per_technician (service_post_id, technician_id),
    
    INDEX idx_consultations_service_post (service_post_id),
    INDEX idx_consultations_technician (technician_id),
    INDEX idx_consultations_status (status),
    INDEX idx_consultations_submitted_at (submitted_at),
    INDEX idx_consultations_responded_at (responded_at),
    INDEX idx_consultations_quoted_price (quoted_price)
);

-- Add constraints for ENUM values
ALTER TABLE consultations 
ADD CONSTRAINT chk_consultations_status 
    CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'));

-- Add constraints for positive values
ALTER TABLE consultations 
ADD CONSTRAINT chk_consultations_quoted_price 
    CHECK (quoted_price >= 0);

-- Add constraint to ensure proposal is not empty
ALTER TABLE consultations 
ADD CONSTRAINT chk_consultations_proposal_not_empty 
    CHECK (CHAR_LENGTH(TRIM(proposal)) > 0);

-- Add constraint to ensure responded_at is set when status is not PENDING
-- Note: This constraint is commented out as it's difficult to enforce at DB level
-- We'll handle this validation in the application layer instead
-- ALTER TABLE consultations 
-- ADD CONSTRAINT chk_consultations_responded_at_when_not_pending 
--     CHECK (
--         (status = 'PENDING' AND responded_at IS NULL) OR 
--         (status IN ('ACCEPTED', 'REJECTED') AND responded_at IS NOT NULL)
--     );

-- Create indexes for better query performance
CREATE INDEX idx_consultations_service_post_status ON consultations(service_post_id, status);
CREATE INDEX idx_consultations_technician_status ON consultations(technician_id, status);
CREATE INDEX idx_consultations_status_submitted_at ON consultations(status, submitted_at);

-- Sample data for testing (optional)
-- Note: This assumes existing service posts and users in the system
-- These would typically be inserted by the application in real use

-- INSERT INTO consultations (service_post_id, technician_id, proposal, quoted_price, notes, status)
-- VALUES 
-- (1, 4, 'Tôi có thể sửa chữa ổ cắm điện cho bạn. Vấn đề có thể do dây điện bị hỏng hoặc ổ cắm bị cháy. Tôi sẽ kiểm tra và thay thế nếu cần thiết.', 180000, 'Bao gồm vật liệu và công lắp đặt', 'PENDING'),
-- (1, 5, 'Với kinh nghiệm 5 năm về điện, tôi sẽ kiểm tra toàn bộ hệ thống điện và sửa chữa ổ cắm một cách an toàn.', 220000, 'Sử dụng vật liệu chất lượng cao, bảo hành 6 tháng', 'PENDING'),
-- (2, 6, 'Lắp đặt máy lạnh 1.5HP với quy trình chuẩn. Bao gồm thi công đường ống đồng, dây điện và khoan tường.', 450000, 'Bảo hành máy 2 năm, thi công 1 năm', 'ACCEPTED'),
-- (3, 7, 'Sửa chữa đường ống nước rò rỉ. Cần kiểm tra để xác định vị trí chính xác và phương pháp sửa chữa phù hợp.', 280000, 'Giá có thể thay đổi tùy theo mức độ hư hỏng', 'PENDING'); 