-- Migration V011: Create Chat System Tables
-- Creates conversations and messages tables for the chat/messaging system
-- Author: Fix4Home Development Team
-- Date: December 2024

-- ================================================================
-- 1. CREATE CONVERSATIONS TABLE
-- ================================================================

CREATE TABLE conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Business Context Links (one of these will be set)
    service_request_id BIGINT NULL,
    service_post_id BIGINT NULL,
    consultation_id BIGINT NULL,
    
    -- Participants (required)
    customer_id BIGINT NOT NULL,
    technician_id BIGINT NOT NULL,
    
    -- Conversation Management
    status ENUM('ACTIVE', 'ARCHIVED', 'BLOCKED') NOT NULL DEFAULT 'ACTIVE',
    last_message_at TIMESTAMP NULL,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign Key Constraints
    FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE SET NULL,
    FOREIGN KEY (service_post_id) REFERENCES service_posts(id) ON DELETE SET NULL,
    FOREIGN KEY (consultation_id) REFERENCES consultations(id) ON DELETE SET NULL,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (technician_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Indexes for Performance
    INDEX idx_conversations_customer (customer_id),
    INDEX idx_conversations_technician (technician_id),
    INDEX idx_conversations_participants (customer_id, technician_id),
    INDEX idx_conversations_service_request (service_request_id),
    INDEX idx_conversations_service_post (service_post_id),
    INDEX idx_conversations_consultation (consultation_id),
    INDEX idx_conversations_status (status),
    INDEX idx_conversations_last_message (last_message_at),
    INDEX idx_conversations_created_at (created_at)
);

-- ================================================================
-- 2. CREATE MESSAGES TABLE
-- ================================================================

CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Message Relationships
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NULL, -- NULL for system messages
    
    -- Message Content
    content TEXT NOT NULL,
    message_type ENUM('TEXT', 'IMAGE', 'LOCATION', 'SYSTEM', 'QUOTATION', 'FILE') NOT NULL DEFAULT 'TEXT',
    attachment_url VARCHAR(500) NULL,
    metadata JSON NULL,
    
    -- Message Status
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key Constraints
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL,
    
    -- Indexes for Performance
    INDEX idx_messages_conversation (conversation_id),
    INDEX idx_messages_sender (sender_id),
    INDEX idx_messages_conversation_time (conversation_id, sent_at DESC),
    INDEX idx_messages_sent_at (sent_at),
    INDEX idx_messages_unread (conversation_id, is_read),
    INDEX idx_messages_type (message_type),
    INDEX idx_messages_unread_user (conversation_id, sender_id, is_read)
);

-- ================================================================
-- 3. ADD CONSTRAINTS AND BUSINESS RULES
-- ================================================================

-- Ensure at least one business context is set in conversations
ALTER TABLE conversations 
ADD CONSTRAINT chk_conversation_context 
CHECK (
    (service_request_id IS NOT NULL AND service_post_id IS NULL AND consultation_id IS NULL) OR
    (service_request_id IS NULL AND service_post_id IS NOT NULL AND consultation_id IS NULL) OR
    (service_request_id IS NULL AND service_post_id IS NULL AND consultation_id IS NOT NULL) OR
    (service_request_id IS NULL AND service_post_id IS NULL AND consultation_id IS NULL)
);

-- Ensure customer and technician are different users
ALTER TABLE conversations 
ADD CONSTRAINT chk_different_participants 
CHECK (customer_id != technician_id);

-- ================================================================
-- 4. ADD COMMENTS FOR DOCUMENTATION
-- ================================================================

-- Table Comments
ALTER TABLE conversations COMMENT = 'Chat conversations between customers and technicians';
ALTER TABLE messages COMMENT = 'Individual messages within conversations';

-- Column Comments for conversations table
ALTER TABLE conversations MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT 'Primary key';
ALTER TABLE conversations MODIFY COLUMN service_request_id BIGINT COMMENT 'Link to service request (if conversation is about a service request)';
ALTER TABLE conversations MODIFY COLUMN service_post_id BIGINT COMMENT 'Link to service post (if conversation is about a service post)';
ALTER TABLE conversations MODIFY COLUMN consultation_id BIGINT COMMENT 'Link to consultation (if conversation is about a consultation)';
ALTER TABLE conversations MODIFY COLUMN customer_id BIGINT NOT NULL COMMENT 'Customer participant in the conversation';
ALTER TABLE conversations MODIFY COLUMN technician_id BIGINT NOT NULL COMMENT 'Technician participant in the conversation';
ALTER TABLE conversations MODIFY COLUMN status ENUM('ACTIVE', 'ARCHIVED', 'BLOCKED') NOT NULL DEFAULT 'ACTIVE' COMMENT 'Conversation status - ACTIVE: can send messages, ARCHIVED: read-only, BLOCKED: disabled';
ALTER TABLE conversations MODIFY COLUMN last_message_at TIMESTAMP COMMENT 'Timestamp of the last message in this conversation';
ALTER TABLE conversations MODIFY COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When the conversation was created';
ALTER TABLE conversations MODIFY COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'When the conversation was last updated';

-- Column Comments for messages table
ALTER TABLE messages MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT 'Primary key';
ALTER TABLE messages MODIFY COLUMN conversation_id BIGINT NOT NULL COMMENT 'Reference to the conversation this message belongs to';
ALTER TABLE messages MODIFY COLUMN sender_id BIGINT COMMENT 'User who sent the message (NULL for system messages)';
ALTER TABLE messages MODIFY COLUMN content TEXT NOT NULL COMMENT 'Message content/text';
ALTER TABLE messages MODIFY COLUMN message_type ENUM('TEXT', 'IMAGE', 'LOCATION', 'SYSTEM', 'QUOTATION', 'FILE') NOT NULL DEFAULT 'TEXT' COMMENT 'Type of message - TEXT: plain text, IMAGE: image attachment, LOCATION: location sharing, SYSTEM: automated message, QUOTATION: price quote, FILE: file attachment';
ALTER TABLE messages MODIFY COLUMN attachment_url VARCHAR(500) COMMENT 'URL to attached file/image';
ALTER TABLE messages MODIFY COLUMN metadata JSON COMMENT 'Additional message metadata (coordinates for location, pricing for quotation, etc.)';
ALTER TABLE messages MODIFY COLUMN is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether the message has been read by the recipient';
ALTER TABLE messages MODIFY COLUMN sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When the message was sent';

-- ================================================================
-- 5. CREATE SAMPLE DATA (FOR DEVELOPMENT/TESTING)
-- ================================================================

-- Note: Sample data should only be inserted in development environment
-- This section can be commented out for production deployments

/*
-- Sample conversation between customer and technician for a service request
INSERT INTO conversations (service_request_id, customer_id, technician_id, status) 
VALUES (1, 1, 2, 'ACTIVE');

-- Sample initial system message
INSERT INTO messages (conversation_id, sender_id, content, message_type) 
VALUES (1, NULL, 'Service request has been accepted. You can now communicate directly!', 'SYSTEM');

-- Sample user message
INSERT INTO messages (conversation_id, sender_id, content, message_type) 
VALUES (1, 1, 'Hi, when can you come to fix the plumbing issue?', 'TEXT');

-- Sample technician response
INSERT INTO messages (conversation_id, sender_id, content, message_type) 
VALUES (1, 2, 'Hello! I can come tomorrow morning around 9 AM. Does that work for you?', 'TEXT');
*/

-- ================================================================
-- 6. MIGRATION COMPLETION LOG
-- ================================================================

-- Log migration completion
INSERT INTO migration_log (version, description, executed_at) 
VALUES ('V011', 'Create Chat System Tables - conversations and messages tables with indexes and constraints', NOW())
ON DUPLICATE KEY UPDATE executed_at = NOW();

-- Migration V011 completed successfully 