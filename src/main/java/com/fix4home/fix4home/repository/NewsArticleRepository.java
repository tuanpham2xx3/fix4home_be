package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.NewsArticle;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    
    /**
     * Find article by slug (unique)
     */
    Optional<NewsArticle> findBySlug(String slug);
    
    /**
     * Find articles by status
     */
    Page<NewsArticle> findByStatus(ArticleStatus status, Pageable pageable);
    
    /**
     * Find articles by author
     */
    Page<NewsArticle> findByAuthor(User author, Pageable pageable);
    
    /**
     * Find articles by author and status
     */
    Page<NewsArticle> findByAuthorAndStatus(User author, ArticleStatus status, Pageable pageable);
    
    /**
     * Find published articles (status = PUBLISHED and publishedAt is not null)
     */
    @Query("SELECT a FROM NewsArticle a WHERE a.status = :status AND a.publishedAt IS NOT NULL ORDER BY a.publishedAt DESC")
    Page<NewsArticle> findPublishedArticles(@Param("status") ArticleStatus status, Pageable pageable);
    
    /**
     * Search articles by title (case-insensitive)
     */
    Page<NewsArticle> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    
    /**
     * Search published articles by title
     */
    @Query("SELECT a FROM NewsArticle a WHERE a.status = :status AND a.publishedAt IS NOT NULL AND LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.publishedAt DESC")
    Page<NewsArticle> searchPublishedArticles(@Param("status") ArticleStatus status, @Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Check if slug exists (excluding current article ID)
     */
    @Query("SELECT COUNT(a) > 0 FROM NewsArticle a WHERE a.slug = :slug AND (:excludeId IS NULL OR a.id != :excludeId)")
    boolean existsBySlugExcludingId(@Param("slug") String slug, @Param("excludeId") Long excludeId);
    
    /**
     * Find article by ID and author
     */
    Optional<NewsArticle> findByIdAndAuthor(Long id, User author);
}

