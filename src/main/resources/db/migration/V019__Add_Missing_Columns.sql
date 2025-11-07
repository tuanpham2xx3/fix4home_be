-- V019: Add missing columns to technician_profiles and feedbacks tables
-- Date: 2025-11-03

-- Add missing columns to technician_profiles table
ALTER TABLE technician_profiles 
ADD COLUMN experience_years INT COMMENT 'Years of experience as integer',
ADD COLUMN description TEXT COMMENT 'Detailed profile description';

-- Add missing column to feedbacks table if not already exists (check separately)
-- ALTER TABLE feedbacks ADD COLUMN reply TEXT COMMENT 'Technician reply to feedback';

-- Add indexes for better query performance
CREATE INDEX idx_technician_profiles_experience_years ON technician_profiles(experience_years);
CREATE INDEX idx_technician_profiles_description ON technician_profiles(description(255));

