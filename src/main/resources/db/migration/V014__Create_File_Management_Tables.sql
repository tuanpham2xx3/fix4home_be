-- ================================================================
-- FILE MANAGEMENT SYSTEM MIGRATION
-- Version: V014
-- Description: Create tables for comprehensive file management
-- Author: Fix4Home Development Team
-- Date: 2024-12-19
-- ================================================================

-- Create file_metadata table
CREATE TABLE file_metadata (
    id BIGINT NOT NULL AUTO_INCREMENT,
    original_filename VARCHAR(255) NOT NULL COMMENT 'Original filename as uploaded',
    stored_filename VARCHAR(255) NOT NULL UNIQUE COMMENT 'Unique stored filename (UUID-based)',
    file_path TEXT NOT NULL COMMENT 'Full file path on storage',
    file_url TEXT NOT NULL COMMENT 'Public URL to access the file',
    content_type VARCHAR(100) NOT NULL COMMENT 'MIME content type',
    file_size BIGINT NOT NULL COMMENT 'File size in bytes',
    file_type VARCHAR(20) NOT NULL COMMENT 'File type category (IMAGE, DOCUMENT, etc.)',
    user_id BIGINT NOT NULL COMMENT 'User who uploaded the file',
    entity_type VARCHAR(50) COMMENT 'Type of entity this file belongs to',
    entity_id BIGINT COMMENT 'ID of the entity this file belongs to',
    description TEXT COMMENT 'User-provided description of the file',
    is_public BOOLEAN DEFAULT FALSE COMMENT 'Whether file is publicly accessible',
    thumbnail_url TEXT COMMENT 'URL to thumbnail (for images)',
    width INT COMMENT 'Image width in pixels',
    height INT COMMENT 'Image height in pixels',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Upload timestamp',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    PRIMARY KEY (id),
    
    -- Foreign key constraints
    CONSTRAINT fk_file_metadata_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE,
    
    -- Check constraints
    CONSTRAINT chk_file_size_positive 
        CHECK (file_size > 0),
    CONSTRAINT chk_image_dimensions 
        CHECK ((width IS NULL AND height IS NULL) OR (width > 0 AND height > 0)),
    CONSTRAINT chk_file_type_enum 
        CHECK (file_type IN ('IMAGE', 'DOCUMENT', 'VIDEO', 'AUDIO', 'OTHER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='File metadata and management information';

-- ================================================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- ================================================================

-- Index for user file queries
CREATE INDEX idx_file_metadata_user_id 
    ON file_metadata(user_id);

-- Index for entity-based file queries  
CREATE INDEX idx_file_metadata_entity_type 
    ON file_metadata(entity_type, entity_id);

-- Index for file type filtering
CREATE INDEX idx_file_metadata_file_type 
    ON file_metadata(file_type);

-- Index for chronological queries
CREATE INDEX idx_file_metadata_created_at 
    ON file_metadata(created_at);

-- Index for public file access
CREATE INDEX idx_file_metadata_public 
    ON file_metadata(is_public, created_at);

-- Composite index for user + type queries
CREATE INDEX idx_file_metadata_user_type 
    ON file_metadata(user_id, file_type, created_at);

-- Index for filename searches
CREATE INDEX idx_file_metadata_original_filename 
    ON file_metadata(original_filename);

-- Unique index on stored filename (already unique constraint, but explicit index)
CREATE UNIQUE INDEX idx_file_metadata_stored_filename 
    ON file_metadata(stored_filename);

-- ================================================================
-- SAMPLE DATA FOR TESTING (Optional - commented out for production)
-- ================================================================

/*
-- Insert sample file metadata for testing
INSERT INTO file_metadata (
    original_filename, stored_filename, file_path, file_url, 
    content_type, file_size, file_type, user_id, entity_type, entity_id,
    description, is_public
) VALUES 
-- Customer avatar
('avatar.jpg', 'f47ac10b-58cc-4372-a567-0e02b2c3d479.jpg', 
 '/uploads/avatars/f47ac10b-58cc-4372-a567-0e02b2c3d479.jpg',
 'http://localhost:8100/api/v1/files/download/f47ac10b-58cc-4372-a567-0e02b2c3d479.jpg',
 'image/jpeg', 153600, 'IMAGE', 1, 'USER_AVATAR', 1, 'User profile picture', true),

-- Service request documentation
('repair_estimate.pdf', '550e8400-e29b-41d4-a716-446655440000.pdf',
 '/uploads/service-requests/550e8400-e29b-41d4-a716-446655440000.pdf',
 'http://localhost:8100/api/v1/files/download/550e8400-e29b-41d4-a716-446655440000.pdf',
 'application/pdf', 2097152, 'DOCUMENT', 1, 'SERVICE_REQUEST', 1, 'Repair cost estimate', false),

-- Before/after photos
('before_repair.jpg', '6ba7b810-9dad-11d1-80b4-00c04fd430c8.jpg',
 '/uploads/service-requests/6ba7b810-9dad-11d1-80b4-00c04fd430c8.jpg',
 'http://localhost:8100/api/v1/files/download/6ba7b810-9dad-11d1-80b4-00c04fd430c8.jpg',
 'image/jpeg', 524288, 'IMAGE', 2, 'SERVICE_REQUEST', 1, 'Photo before repair work', false),

('after_repair.jpg', '6ba7b811-9dad-11d1-80b4-00c04fd430c8.jpg',
 '/uploads/service-requests/6ba7b811-9dad-11d1-80b4-00c04fd430c8.jpg',
 'http://localhost:8100/api/v1/files/download/6ba7b811-9dad-11d1-80b4-00c04fd430c8.jpg',
 'image/jpeg', 612352, 'IMAGE', 2, 'SERVICE_REQUEST', 1, 'Photo after repair completion', false);
*/

-- ================================================================
-- VERIFICATION QUERIES
-- ================================================================

-- Verify table creation
SELECT 'file_metadata table created successfully' as status;

-- Show table structure
-- DESCRIBE file_metadata;

-- Show indexes
-- SHOW INDEX FROM file_metadata;

-- ================================================================
-- COMMENTS AND DOCUMENTATION
-- ================================================================

/*
FILE MANAGEMENT SYSTEM FEATURES:

1. COMPREHENSIVE METADATA STORAGE
   - Original and stored filenames
   - File paths and public URLs
   - Content type detection and validation
   - File size tracking
   - Categorization by file type

2. ENTITY ASSOCIATION
   - Link files to specific entities (SERVICE_REQUEST, USER_AVATAR, etc.)
   - Flexible entity type system for future expansion
   - Optional entity association for general files

3. ACCESS CONTROL
   - User ownership tracking
   - Public/private file designation
   - Permission-based access control

4. IMAGE PROCESSING SUPPORT
   - Width/height dimension storage
   - Thumbnail URL tracking
   - Image optimization metadata

5. PERFORMANCE OPTIMIZATION
   - Strategic indexing for common queries
   - Efficient file lookup by various criteria
   - Optimized for user-based and entity-based queries

6. AUDIT TRAIL
   - Creation and update timestamps
   - User upload tracking
   - File lifecycle management

USAGE PATTERNS:
- User avatar management
- Service request documentation
- Before/after repair photos
- Technician certification documents
- System-wide file attachments

SECURITY CONSIDERATIONS:
- UUID-based stored filenames prevent guessing
- User ownership verification
- Public/private access control
- File type validation
- Size limit enforcement
*/
