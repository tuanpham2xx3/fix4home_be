-- V026__Create_Chatbot_Tables.sql
-- Simple version without BOM for MySQL Workbench

USE fix4home_db;

CREATE TABLE chatbot_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(8) NOT NULL UNIQUE COMMENT '8-digit session ID for n8n',
    user_id BIGINT NOT NULL COMMENT 'User who owns this session',
    message_count INT NOT NULL DEFAULT 0 COMMENT 'Number of message pairs (user + bot) in this session',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether this session is currently active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_chatbot_sessions_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_chatbot_sessions_session_id (session_id),
    INDEX idx_chatbot_sessions_user_id (user_id),
    INDEX idx_chatbot_sessions_user_active (user_id, is_active, created_at DESC),
    INDEX idx_chatbot_sessions_created_at (created_at)
) COMMENT = 'Chatbot sessions for managing conversation context with n8n';

CREATE TABLE chatbot_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(8) NOT NULL COMMENT 'Session ID this message belongs to',
    user_id BIGINT NOT NULL COMMENT 'User who sent/received this message',
    message TEXT NOT NULL COMMENT 'User message or bot response',
    message_type ENUM('USER', 'BOT') NOT NULL COMMENT 'Type: USER message or BOT response',
    pair_sequence INT NOT NULL COMMENT 'Sequence number of the message pair (1, 2, 3, ...)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chatbot_messages_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_chatbot_messages_session_id (session_id),
    INDEX idx_chatbot_messages_user_id (user_id),
    INDEX idx_chatbot_messages_session_sequence (session_id, pair_sequence, created_at),
    INDEX idx_chatbot_messages_user_created (user_id, created_at DESC),
    INDEX idx_chatbot_messages_type (message_type)
) COMMENT = 'Chat history between users and chatbot via n8n';

