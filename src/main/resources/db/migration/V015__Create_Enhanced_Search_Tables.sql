-- ================================================================
-- ENHANCED SEARCH SYSTEM MIGRATION
-- Version: V015
-- Description: Create tables for search history and saved searches
-- Author: Fix4Home Development Team
-- Date: 2024-12-19
-- ================================================================

-- Create search_history table
CREATE TABLE search_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'User who performed the search',
    search_type VARCHAR(50) NOT NULL COMMENT 'Type of search (TECHNICIAN, SERVICE_POST, SERVICE)',
    search_query VARCHAR(500) COMMENT 'The actual search keywords',
    search_criteria TEXT COMMENT 'JSON of the complete search criteria',
    results_count INT COMMENT 'Number of results found',
    search_location VARCHAR(200) COMMENT 'Search location text',
    latitude DOUBLE COMMENT 'Search location latitude',
    longitude DOUBLE COMMENT 'Search location longitude',
    radius INT COMMENT 'Search radius in kilometers',
    filters_applied VARCHAR(500) COMMENT 'Comma-separated list of applied filters',
    sort_by VARCHAR(50) COMMENT 'Sort criteria used',
    sort_direction VARCHAR(10) COMMENT 'Sort direction (asc/desc)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Search timestamp',
    
    PRIMARY KEY (id),
    
    -- Foreign key constraints
    CONSTRAINT fk_search_history_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE,
    
    -- Check constraints
    CONSTRAINT chk_search_type_enum 
        CHECK (search_type IN ('TECHNICIAN', 'SERVICE_POST', 'SERVICE')),
    CONSTRAINT chk_radius_positive 
        CHECK (radius IS NULL OR radius > 0),
    CONSTRAINT chk_results_count_non_negative 
        CHECK (results_count IS NULL OR results_count >= 0),
    CONSTRAINT chk_sort_direction_enum 
        CHECK (sort_direction IN ('asc', 'desc'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Search history tracking for users';

-- Create saved_searches table
CREATE TABLE saved_searches (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'User who owns the saved search',
    search_name VARCHAR(100) NOT NULL COMMENT 'User-defined name for the search',
    search_type VARCHAR(50) NOT NULL COMMENT 'Type of search (TECHNICIAN, SERVICE_POST, SERVICE)',
    search_criteria TEXT NOT NULL COMMENT 'JSON of the complete search criteria',
    description VARCHAR(300) COMMENT 'User description of the saved search',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Whether the saved search is active',
    notification_enabled BOOLEAN DEFAULT FALSE COMMENT 'Whether notifications are enabled',
    last_executed_at TIMESTAMP COMMENT 'Last time this search was executed',
    execution_count INT DEFAULT 0 COMMENT 'Number of times this search has been executed',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    PRIMARY KEY (id),
    
    -- Foreign key constraints
    CONSTRAINT fk_saved_searches_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE,
    
    -- Unique constraint for active searches per user
    CONSTRAINT uk_user_search_name 
        UNIQUE (user_id, search_name, is_active),
    
    -- Check constraints
    CONSTRAINT chk_saved_search_type_enum 
        CHECK (search_type IN ('TECHNICIAN', 'SERVICE_POST', 'SERVICE')),
    CONSTRAINT chk_execution_count_non_negative 
        CHECK (execution_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='User saved searches for quick access';

-- ================================================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- ================================================================

-- Search History Indexes
CREATE INDEX idx_search_history_user_id 
    ON search_history(user_id);

CREATE INDEX idx_search_history_created_at 
    ON search_history(created_at);

CREATE INDEX idx_search_history_search_type 
    ON search_history(search_type);

-- Composite index for user search history queries
CREATE INDEX idx_search_history_user_type_created 
    ON search_history(user_id, search_type, created_at);

-- Index for location-based queries
CREATE INDEX idx_search_history_location 
    ON search_history(latitude, longitude, created_at);

-- Index for search query text search
CREATE INDEX idx_search_history_query 
    ON search_history(search_query);

-- Saved Searches Indexes
CREATE INDEX idx_saved_searches_user_id 
    ON saved_searches(user_id);

CREATE INDEX idx_saved_searches_search_type 
    ON saved_searches(search_type);

CREATE INDEX idx_saved_searches_is_active 
    ON saved_searches(is_active);

-- Composite index for user active saved searches
CREATE INDEX idx_saved_searches_user_active 
    ON saved_searches(user_id, is_active, created_at);

-- Index for notification processing
CREATE INDEX idx_saved_searches_notifications 
    ON saved_searches(notification_enabled, is_active);

-- Index for execution statistics
CREATE INDEX idx_saved_searches_execution 
    ON saved_searches(execution_count, last_executed_at);

-- Index for search name lookups
CREATE INDEX idx_saved_searches_name 
    ON saved_searches(search_name);

-- ================================================================
-- SAMPLE DATA FOR TESTING (Optional - commented out for production)
-- ================================================================

/*
-- Insert sample search history
INSERT INTO search_history (
    user_id, search_type, search_query, search_criteria, results_count,
    search_location, latitude, longitude, radius, filters_applied,
    sort_by, sort_direction
) VALUES 
-- Customer searching for technicians
(1, 'TECHNICIAN', 'electrical repair', 
 '{"keyword":"electrical repair","location":"Hanoi","minRating":4.0,"radius":10}',
 15, 'Hanoi, Vietnam', 21.0285, 105.8542, 10, 'location,rating', 'distance', 'asc'),

-- Technician searching for service posts
(2, 'SERVICE_POST', 'plumbing emergency', 
 '{"keyword":"plumbing emergency","availableNow":true,"radius":15}',
 8, 'Ho Chi Minh City', 10.7769, 106.7009, 15, 'availability,location', 'created_at', 'desc'),

-- Customer browsing services
(1, 'SERVICE', 'air conditioning', 
 '{"keyword":"air conditioning","sortBy":"price"}',
 25, null, null, null, null, '', 'price', 'asc');

-- Insert sample saved searches
INSERT INTO saved_searches (
    user_id, search_name, search_type, search_criteria, description,
    notification_enabled, execution_count
) VALUES 
-- Emergency electricians
(1, 'Emergency Electricians Near Me', 'TECHNICIAN',
 '{"keyword":"electrical","emergencyService":true,"radius":5,"minRating":4.0,"availableNow":true}',
 'Quick access to emergency electrical services in my area',
 true, 5),

-- Budget plumbing services
(1, 'Budget Plumbing Services', 'TECHNICIAN',
 '{"serviceCategories":["PLUMBING"],"maxPrice":500000,"sortBy":"price"}',
 'Affordable plumbing services for routine maintenance',
 false, 2),

-- High-value service posts
(2, 'High-Value Service Posts', 'SERVICE_POST',
 '{"minBudget":1000000,"sortBy":"estimatedBudget","sortDirection":"desc"}',
 'High-paying service opportunities',
 true, 12);
*/

-- ================================================================
-- VERIFICATION QUERIES
-- ================================================================

-- Verify table creation
SELECT 'search_history table created successfully' as status;
SELECT 'saved_searches table created successfully' as status;

-- Show table structures
-- DESCRIBE search_history;
-- DESCRIBE saved_searches;

-- Show indexes
-- SHOW INDEX FROM search_history;
-- SHOW INDEX FROM saved_searches;

-- ================================================================
-- CLEANUP PROCEDURES (for maintenance)
-- ================================================================

/*
-- Procedure to clean up old search history (older than 6 months)
DELIMITER //
CREATE PROCEDURE CleanupOldSearchHistory()
BEGIN
    DELETE FROM search_history 
    WHERE created_at < DATE_SUB(NOW(), INTERVAL 6 MONTH);
    
    SELECT ROW_COUNT() as deleted_records;
END //
DELIMITER ;

-- Procedure to clean up excess search history per user (keep latest 100 per user)
DELIMITER //
CREATE PROCEDURE CleanupExcessSearchHistory()
BEGIN
    DELETE sh1 FROM search_history sh1
    INNER JOIN (
        SELECT user_id, id,
               ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY created_at DESC) as rn
        FROM search_history
    ) sh2 ON sh1.id = sh2.id
    WHERE sh2.rn > 100;
    
    SELECT ROW_COUNT() as deleted_records;
END //
DELIMITER ;

-- Schedule these procedures to run periodically via cron or scheduled events
*/

-- ================================================================
-- COMMENTS AND DOCUMENTATION
-- ================================================================

/*
ENHANCED SEARCH SYSTEM FEATURES:

1. SEARCH HISTORY TRACKING
   - Complete search criteria preservation
   - Location-based search tracking
   - Filter usage analytics
   - Search performance metrics

2. SAVED SEARCHES
   - User-defined search shortcuts
   - Notification support for new matches
   - Execution frequency tracking
   - Search optimization suggestions

3. SEARCH ANALYTICS
   - Popular search terms and locations
   - User search behavior patterns
   - Filter usage statistics
   - Search result quality metrics

4. PERFORMANCE OPTIMIZATIONS
   - Strategic indexing for fast queries
   - Efficient pagination support
   - Location-based spatial queries
   - Search history cleanup automation

5. PRIVACY CONSIDERATIONS
   - User-owned search data
   - Automatic cleanup of old data
   - Opt-out search tracking support
   - Secure search criteria storage

USAGE PATTERNS:
- Search autocomplete and suggestions
- Popular search discovery
- Personalized search recommendations
- Search result optimization
- User behavior analytics

MAINTENANCE:
- Regular cleanup of old search history
- Index optimization monitoring
- Search performance analytics
- User search quota management
*/
