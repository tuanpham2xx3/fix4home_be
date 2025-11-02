
-- USERS
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    role ENUM('customer', 'technician', 'admin') NOT NULL,
    status ENUM('active', 'inactive') DEFAULT 'active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- CUSTOMER PROFILES
CREATE TABLE customer_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(100),
    gender ENUM('male', 'female', 'other'),
    dob DATE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- TECHNICIAN PROFILES
CREATE TABLE technician_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(100),
    skills TEXT,
    experience VARCHAR(255),
    rating FLOAT DEFAULT 0,
    status ENUM('active', 'inactive') DEFAULT 'active',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- SKILLS
CREATE TABLE skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- TECHNICIAN_SKILLS (many-to-many)
CREATE TABLE technician_skills (
    technician_profile_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (technician_profile_id, skill_id),
    FOREIGN KEY (technician_profile_id) REFERENCES technician_profiles(id),
    FOREIGN KEY (skill_id) REFERENCES skills(id)
);

-- ADDRESSES (Bổ sung hỗ trợ khách vãng lai)
CREATE TABLE addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    recipient_name VARCHAR(100) NULL,
    recipient_phone VARCHAR(20) NULL,
    address_line VARCHAR(255),
    ward VARCHAR(100),
    district VARCHAR(100),
    city VARCHAR(100),
    latitude DECIMAL(10, 6),
    longitude DECIMAL(10, 6),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- SERVICES
CREATE TABLE services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    base_price DECIMAL(12,2),
    status ENUM('active', 'inactive') DEFAULT 'active'
);

-- SERVICE REQUESTS
CREATE TABLE service_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    technician_id BIGINT,
    address_id BIGINT NOT NULL,
    description TEXT,
    status ENUM('pending', 'assigned', 'in_progress', 'done', 'cancelled') DEFAULT 'pending',
    scheduled_time DATETIME,
    completed_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    price DECIMAL(12,2) NULL,
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (service_id) REFERENCES services(id),
    FOREIGN KEY (technician_id) REFERENCES users(id),
    FOREIGN KEY (address_id) REFERENCES addresses(id)
);

-- SERVICE REQUEST LOGS
CREATE TABLE service_request_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_request_id BIGINT NOT NULL,
    old_status ENUM('pending', 'assigned', 'in_progress', 'done', 'cancelled'),
    new_status ENUM('pending', 'assigned', 'in_progress', 'done', 'cancelled') NOT NULL,
    changed_by BIGINT,
    changed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (service_request_id) REFERENCES service_requests(id),
    FOREIGN KEY (changed_by) REFERENCES users(id)
);

-- FEEDBACKS
CREATE TABLE feedbacks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_request_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    technician_id BIGINT NOT NULL,
    rating INT CHECK(rating >= 1 AND rating <= 5),
    comment TEXT,
    reply TEXT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (service_request_id) REFERENCES service_requests(id),
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (technician_id) REFERENCES users(id)
);

-- PAYMENTS
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_request_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    method ENUM('cash', 'credit_card', 'bank_transfer', 'vnpay') NOT NULL,
    status ENUM('pending', 'paid', 'failed') DEFAULT 'pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    payment_time DATETIME NULL,
    FOREIGN KEY (service_request_id) REFERENCES service_requests(id)
);

-- NOTIFICATIONS
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255),
    message TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
