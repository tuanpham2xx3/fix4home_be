-- V024__Create_News_Articles_Table.sql
-- Migration script for News Articles Management System
-- Author: Fix4Home Development Team

-- ================================================================
-- CREATE NEWS ARTICLES TABLE
-- ================================================================
CREATE TABLE news_articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    title VARCHAR(500) NOT NULL,
    short_description TEXT,
    slug VARCHAR(500) NOT NULL UNIQUE,
    
    -- Content stored as JSON (structured content blocks)
    content_json TEXT NOT NULL,
    
    -- Hero image metadata stored as JSON
    hero_image_json TEXT,
    
    -- SEO fields
    meta_description VARCHAR(500),
    meta_keywords VARCHAR(500),
    
    -- Article status
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    
    -- Sections data stored as JSON
    sections_json TEXT,
    
    -- Contact information stored as JSON
    contact_info_json TEXT,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,
    
    -- Foreign key constraint
    CONSTRAINT fk_news_articles_author 
        FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE RESTRICT,
    
    -- Indexes for performance
    INDEX idx_news_articles_slug (slug),
    INDEX idx_news_articles_status (status),
    INDEX idx_news_articles_author (author_id),
    INDEX idx_news_articles_created_at (created_at),
    INDEX idx_news_articles_published_at (published_at),
    INDEX idx_news_articles_status_published (status, published_at)
);

-- Add constraint for ENUM values
ALTER TABLE news_articles 
ADD CONSTRAINT chk_news_articles_status 
    CHECK (status IN ('DRAFT', 'PUBLISHED', 'UNPUBLISHED'));

