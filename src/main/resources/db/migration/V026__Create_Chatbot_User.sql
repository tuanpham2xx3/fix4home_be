-- Migration V026: Create Chatbot User
-- Creates a system chatbot user for automated support
-- Author: Fix4Home Development Team
-- Date: January 2025

-- ================================================================
-- 1. CREATE CHATBOT USER
-- ================================================================

-- Insert chatbot user with ADMIN role
-- Password is a secure random hash (not used for login, but required by schema)
-- Status is ACTIVE so it can participate in conversations
INSERT INTO users (username, email, password, role, status, created_at, updated_at)
VALUES (
    'chatbot_support',
    'chatbot@fix4home.com',
    '$2a$10$CHATBOT.PASSWORD.HASH.NOT.USED.FOR.LOGIN.REQUIRED.BY.SCHEMA',
    'ADMIN',
    'ACTIVE',
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE 
    username = 'chatbot_support',
    email = 'chatbot@fix4home.com',
    role = 'ADMIN',
    status = 'ACTIVE',
    updated_at = NOW();

-- ================================================================
-- 2. MIGRATION COMPLETION LOG
-- ================================================================

-- Log migration completion
INSERT INTO migration_log (version, description, executed_at) 
VALUES ('V026', 'Create Chatbot User - System chatbot for automated support', NOW())
ON DUPLICATE KEY UPDATE executed_at = NOW();

-- Migration V026 completed successfully

