-- V026__Create_Chatbot_Tables.sql
-- Migration script for Chatbot N8N Integration System
-- Author: Fix4Home Development Team

-- ================================================================
-- 1. CREATE CHATBOT_SESSIONS TABLE
-- ================================================================
CREATE TABLE chatbot_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Session identification
    session_id VARCHAR(8) NOT NULL UNIQUE COMMENT '8-digit session ID for n8n',
    
    -- User association
    user_id BIGINT NOT NULL COMMENT 'User who owns this session',
    
    -- Session management
    message_count INT NOT NULL DEFAULT 0 COMMENT 'Number of message pairs (user + bot) in this session',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether this session is currently active',
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_chatbot_sessions_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_chatbot_sessions_session_id (session_id),
    INDEX idx_chatbot_sessions_user_id (user_id),
    INDEX idx_chatbot_sessions_user_active (user_id, is_active, created_at DESC),
    INDEX idx_chatbot_sessions_created_at (created_at)
) COMMENT = 'Chatbot sessions for managing conversation context with n8n';

-- ================================================================
-- 2. CREATE CHATBOT_MESSAGES TABLE
-- ================================================================
CREATE TABLE chatbot_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Session and user association
    session_id VARCHAR(8) NOT NULL COMMENT 'Session ID this message belongs to',
    user_id BIGINT NOT NULL COMMENT 'User who sent/received this message',
    
    -- Message content
    message TEXT NOT NULL COMMENT 'User message or bot response',
    message_type ENUM('USER', 'BOT') NOT NULL COMMENT 'Type: USER message or BOT response',
    
    -- Message pair tracking
    pair_sequence INT NOT NULL COMMENT 'Sequence number of the message pair (1, 2, 3, ...)',
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_chatbot_messages_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_chatbot_messages_session_id (session_id),
    INDEX idx_chatbot_messages_user_id (user_id),
    INDEX idx_chatbot_messages_session_sequence (session_id, pair_sequence, created_at),
    INDEX idx_chatbot_messages_user_created (user_id, created_at DESC),
    INDEX idx_chatbot_messages_type (message_type)
) COMMENT = 'Chat history between users and chatbot via n8n';

-- ================================================================
-- 3. ADD COLUMN COMMENTS FOR DOCUMENTATION
-- ================================================================

-- chatbot_sessions table comments
ALTER TABLE chatbot_sessions MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT 'Primary key';
ALTER TABLE chatbot_sessions MODIFY COLUMN session_id VARCHAR(8) NOT NULL COMMENT '8-digit unique session identifier for n8n webhook';
ALTER TABLE chatbot_sessions MODIFY COLUMN user_id BIGINT NOT NULL COMMENT 'User who owns this chatbot session';
ALTER TABLE chatbot_sessions MODIFY COLUMN message_count INT NOT NULL DEFAULT 0 COMMENT 'Number of complete message pairs (user message + bot response)';
ALTER TABLE chatbot_sessions MODIFY COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Active sessions are current, inactive sessions are completed (reached 5 pairs)';
ALTER TABLE chatbot_sessions MODIFY COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When the session was created';
ALTER TABLE chatbot_sessions MODIFY COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'When the session was last updated';

-- chatbot_messages table comments
ALTER TABLE chatbot_messages MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT 'Primary key';
ALTER TABLE chatbot_messages MODIFY COLUMN session_id VARCHAR(8) NOT NULL COMMENT 'Session ID this message belongs to';
ALTER TABLE chatbot_messages MODIFY COLUMN user_id BIGINT NOT NULL COMMENT 'User associated with this message';
ALTER TABLE chatbot_messages MODIFY COLUMN message TEXT NOT NULL COMMENT 'The actual message content';
ALTER TABLE chatbot_messages MODIFY COLUMN message_type ENUM('USER', 'BOT') NOT NULL COMMENT 'USER: message from user, BOT: response from chatbot';
ALTER TABLE chatbot_messages MODIFY COLUMN pair_sequence INT NOT NULL COMMENT 'Sequence number of the message pair (1st pair=1, 2nd pair=2, etc.)';
ALTER TABLE chatbot_messages MODIFY COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When the message was created';

