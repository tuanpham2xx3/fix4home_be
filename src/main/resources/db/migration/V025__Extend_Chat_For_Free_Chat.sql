-- Migration V025: Extend Chat System for Free Chat
-- Adds conversation_type field to support free chat and chatbot conversations
-- Author: Fix4Home Development Team
-- Date: January 2025

-- ================================================================
-- 1. ADD CONVERSATION_TYPE COLUMN
-- ================================================================

ALTER TABLE conversations 
ADD COLUMN conversation_type ENUM('BUSINESS', 'FREE', 'CHATBOT') NOT NULL DEFAULT 'BUSINESS' 
COMMENT 'Type of conversation - BUSINESS: linked to business context, FREE: free chat between users, CHATBOT: conversation with chatbot';

-- ================================================================
-- 2. UPDATE EXISTING CONVERSATIONS
-- ================================================================

-- All existing conversations are business-related
UPDATE conversations 
SET conversation_type = 'BUSINESS' 
WHERE conversation_type = 'BUSINESS' OR conversation_type IS NULL;

-- ================================================================
-- 3. ADD INDEX FOR CONVERSATION TYPE
-- ================================================================

CREATE INDEX idx_conversations_type ON conversations(conversation_type);

-- ================================================================
-- 4. UPDATE COMMENTS
-- ================================================================

ALTER TABLE conversations 
MODIFY COLUMN conversation_type ENUM('BUSINESS', 'FREE', 'CHATBOT') NOT NULL DEFAULT 'BUSINESS' 
COMMENT 'Type of conversation - BUSINESS: linked to business context, FREE: free chat between users, CHATBOT: conversation with chatbot';

-- ================================================================
-- 5. MIGRATION COMPLETION LOG
-- ================================================================

-- Log migration completion
INSERT INTO migration_log (version, description, executed_at) 
VALUES ('V025', 'Extend Chat System for Free Chat - Add conversation_type field', NOW())
ON DUPLICATE KEY UPDATE executed_at = NOW();

-- Migration V025 completed successfully

