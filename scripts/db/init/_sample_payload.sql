-- FIX4HOME sample payload (copied from docs/database/sample_data.sql)
-- This file is executed by 02_sample_data.sql via SOURCE

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE notifications;
TRUNCATE TABLE feedbacks;
TRUNCATE TABLE payments;
TRUNCATE TABLE refresh_tokens;
TRUNCATE TABLE messages;
TRUNCATE TABLE conversations;
TRUNCATE TABLE complaints;
TRUNCATE TABLE consultations;
TRUNCATE TABLE service_post_responses;
TRUNCATE TABLE service_posts;
TRUNCATE TABLE service_request_logs;
TRUNCATE TABLE service_requests;
TRUNCATE TABLE technician_skills;
TRUNCATE TABLE technician_profiles;
TRUNCATE TABLE customer_profiles;
TRUNCATE TABLE addresses;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO users (username, password, email, phone_number, role, status)
VALUES
('admin', 'admin123', 'admin@fix4home.local', '0900000000', 'ADMIN', 'ACTIVE'),
('alice', 'alice123', 'alice@fix4home.local', '0901000001', 'CUSTOMER', 'ACTIVE'),
('bobtech', 'bobtech123', 'bob@fix4home.local', '0902000002', 'TECHNICIAN', 'ACTIVE');

SET @adminId = (SELECT id FROM users WHERE username = 'admin');
SET @aliceId = (SELECT id FROM users WHERE username = 'alice');
SET @bobId   = (SELECT id FROM users WHERE username = 'bobtech');

INSERT INTO addresses (user_id, recipient_name, recipient_phone, address_line, ward, district, city, latitude, longitude)
VALUES
(@aliceId, 'Alice', '0901000001', '123 Đường A', 'Phường 1', 'Quận 1', 'TP. HCM', 10.776889, 106.700806),
(@bobId,   'Bob Tech', '0902000002', '45/6 Đường B', 'Phường 7', 'Quận 3', 'TP. HCM', 10.784000, 106.695000);

SET @aliceAddr = LAST_INSERT_ID() - 1;
SET @bobAddr   = LAST_INSERT_ID();

INSERT INTO customer_profiles (user_id, full_name, date_of_birth, gender)
VALUES (@aliceId, 'Alice Nguyen', '1996-05-20', 'FEMALE');

INSERT INTO technician_profiles (
  user_id, full_name, skills, experience, rating, status,
  is_online, last_seen_at, current_latitude, current_longitude, current_address, working_radius,
  verification_documents
) VALUES (
  @bobId, 'Bob Technician', 'Điện dân dụng; Điều hòa', '5 năm kinh nghiệm', 4.6, 'ACTIVE',
  TRUE, NOW(), 10.783500, 106.698900, 'Quận 3, TP. HCM', 15,
  'id_card.png;certificate.pdf'
);

SET @techProfileId = (SELECT id FROM technician_profiles WHERE user_id = @bobId);

INSERT INTO services (name, description, base_price, status)
SELECT 'Sửa chữa điện', 'Sửa chữa hệ thống điện trong nhà', 200000, 'ACTIVE' WHERE NOT EXISTS (
  SELECT 1 FROM services WHERE name = 'Sửa chữa điện'
);

INSERT INTO skills (name, description, status)
SELECT 'Điện dân dụng', 'Kiến thức về hệ thống điện dân dụng', 'ACTIVE' WHERE NOT EXISTS (
  SELECT 1 FROM skills WHERE name = 'Điện dân dụng'
);

SET @svcElectric = (SELECT id FROM services WHERE name = 'Sửa chữa điện');
SET @skillElectric = (SELECT id FROM skills WHERE name = 'Điện dân dụng');

INSERT IGNORE INTO technician_skills (technician_id, skill_id, years_experience, certification_level)
VALUES (@techProfileId, @skillElectric, 5, 'ADVANCED');

INSERT INTO service_requests (
  customer_id, service_id, technician_id, address_id, description, status, scheduled_time, price
) VALUES (
  @aliceId, @svcElectric, @bobId, @aliceAddr, 'Khắc phục chập điện phòng khách', 'ACCEPTED', NOW() + INTERVAL 1 DAY, 350000
);

SET @reqId = LAST_INSERT_ID();

INSERT INTO service_request_logs (service_request_id, old_status, new_status, changed_by)
VALUES
(@reqId, 'PENDING', 'ACCEPTED', @bobId),
(@reqId, 'ACCEPTED', 'IN_PROGRESS', @bobId);

INSERT INTO service_posts (
  customer_id, service_id, address_id, title, description, estimated_budget, preferred_time, type, status, max_technicians, expires_at
) VALUES (
  @aliceId, @svcElectric, @aliceAddr, 'Lắp thêm ổ cắm', 'Cần lắp thêm 2 ổ cắm tại phòng ngủ', 300000, NOW() + INTERVAL 2 DAY,
  'SCHEDULED', 'POSTED', 5, NOW() + INTERVAL 3 DAY
);

SET @postId = LAST_INSERT_ID();

INSERT INTO service_post_responses (
  service_post_id, technician_id, message, quoted_price, estimated_duration, proposed_time, is_selected
) VALUES (
  @postId, @bobId, 'Có thể làm trong buổi chiều', 280000, 2, NOW() + INTERVAL 2 DAY, TRUE
);

INSERT INTO consultations (
  service_post_id, technician_id, proposal, quoted_price, notes, status
) VALUES (
  @postId, @bobId, 'Đề xuất dùng ổ cắm chịu tải 16A', 320000, 'Bảo hành 6 tháng', 'ACCEPTED'
);

INSERT INTO complaints (
  service_request_id, complainant_id, accused_id, reason, description, status, resolved_by
) VALUES (
  @reqId, @aliceId, @bobId, 'Đến trễ', 'Kỹ thuật viên đến trễ 30 phút so với lịch hẹn', 'RESOLVED', @adminId
);

INSERT INTO conversations (
  service_request_id, service_post_id, consultation_id, customer_id, technician_id, status, last_message_at
) VALUES (
  @reqId, @postId, (SELECT id FROM consultations WHERE service_post_id = @postId AND technician_id = @bobId LIMIT 1),
  @aliceId, @bobId, 'ACTIVE', NOW()
);

SET @convId = LAST_INSERT_ID();

INSERT INTO messages (conversation_id, sender_id, content, message_type, is_read)
VALUES
(@convId, @aliceId, 'Chào anh, khi nào anh qua được ạ?', 'TEXT', FALSE),
(@convId, @bobId, 'Chiều nay 3h nhé!', 'TEXT', FALSE);

INSERT INTO payments (service_request_id, amount, payment_method, status, transaction_id, notes, paid_at)
VALUES (@reqId, 350000, 'CASH', 'COMPLETED', 'TXN-DEMO-001', 'Thanh toán sau khi hoàn thành', NOW());

INSERT INTO feedbacks (service_request_id, rating, comment)
VALUES (@reqId, 5, 'Làm việc tốt, nhiệt tình');

INSERT INTO notifications (user_id, title, message, type, is_read)
VALUES
(@aliceId, 'Xác nhận đơn', 'Đơn dịch vụ của bạn đã được xác nhận', 'SUCCESS', TRUE),
(@bobId, 'Đơn mới', 'Bạn có một đơn dịch vụ mới', 'INFO', FALSE);

-- End of sample payload

