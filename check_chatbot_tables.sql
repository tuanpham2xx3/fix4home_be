-- Script to check if chatbot tables exist
-- Run this in MySQL to verify tables

USE fix4home_db;

-- Check if chatbot_sessions table exists
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✓ Table chatbot_sessions EXISTS'
        ELSE '✗ Table chatbot_sessions DOES NOT EXIST'
    END AS chatbot_sessions_status
FROM information_schema.tables 
WHERE table_schema = 'fix4home_db' 
  AND table_name = 'chatbot_sessions';

-- Check if chatbot_messages table exists
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✓ Table chatbot_messages EXISTS'
        ELSE '✗ Table chatbot_messages DOES NOT EXIST'
    END AS chatbot_messages_status
FROM information_schema.tables 
WHERE table_schema = 'fix4home_db' 
  AND table_name = 'chatbot_messages';

-- Show table structure if exists
SHOW TABLES LIKE 'chatbot_%';

-- Show table details
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME,
    UPDATE_TIME
FROM information_schema.tables 
WHERE table_schema = 'fix4home_db' 
  AND table_name IN ('chatbot_sessions', 'chatbot_messages');

