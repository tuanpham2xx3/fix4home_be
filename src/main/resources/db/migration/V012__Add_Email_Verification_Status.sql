-- V012__Add_Email_Verification_Status.sql
-- Migration script to add PENDING_EMAIL_VERIFICATION status to users table
-- Author: Fix4Home Development Team

-- ================================================================
-- ADD PENDING_EMAIL_VERIFICATION STATUS TO USERS TABLE
-- ================================================================

-- Add the new enum value to the status column
ALTER TABLE users 
MODIFY COLUMN status ENUM('ACTIVE', 'INACTIVE', 'PENDING_APPROVAL', 'REJECTED', 'PENDING_EMAIL_VERIFICATION') 
NOT NULL DEFAULT 'ACTIVE'; 