package com.fix4home.fix4home.model.entity;

import com.fix4home.fix4home.model.enums.ArticleStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * News Article entity represents articles/news posts in the system
 * Supports rich text content with structured JSON format
 */
@Entity
@Table(name = "news_articles", indexes = {
    @Index(name = "idx_news_articles_slug", columnList = "slug"),
    @Index(name = "idx_news_articles_status", columnList = "status"),
    @Index(name = "idx_news_articles_author", columnList = "author_id"),
    @Index(name = "idx_news_articles_created_at", columnList = "created_at"),
    @Index(name = "idx_news_articles_published_at", columnList = "published_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @NotNull(message = "Author is required")
    private User author;
    
    @Column(nullable = false, length = 500)
    @NotBlank(message = "Title is required")
    private String title;
    
    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;
    
    @Column(nullable = false, length = 500, unique = true)
    @NotBlank(message = "Slug is required")
    private String slug;
    
    @Column(name = "content_json", columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Content is required")
    private String contentJson;
    
    @Column(name = "hero_image_json", columnDefinition = "TEXT")
    private String heroImageJson;
    
    @Column(name = "meta_description", length = 500)
    private String metaDescription;
    
    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ArticleStatus status = ArticleStatus.DRAFT;
    
    @Column(name = "sections_json", columnDefinition = "TEXT")
    private String sectionsJson;
    
    @Column(name = "contact_info_json", columnDefinition = "TEXT")
    private String contactInfoJson;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    // Helper methods
    public boolean isPublished() {
        return this.status == ArticleStatus.PUBLISHED;
    }
    
    public boolean isDraft() {
        return this.status == ArticleStatus.DRAFT;
    }
    
    public boolean canBePublished() {
        return this.status == ArticleStatus.DRAFT || this.status == ArticleStatus.UNPUBLISHED;
    }
}

