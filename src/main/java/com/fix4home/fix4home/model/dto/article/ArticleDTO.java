package com.fix4home.fix4home.model.dto.article;

import com.fix4home.fix4home.model.enums.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Main DTO for News Article response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleDTO {
    
    private Long id;
    private String title;
    private String shortDescription;
    private String slug;
    
    // Structured content
    private ContentStructureDTO content;
    
    // Hero image
    private HeroImageDTO heroImage;
    
    // SEO fields
    private String metaDescription;
    private String metaKeywords;
    
    // Status
    private ArticleStatus status;
    
    // Sections
    private List<SectionDTO> sections;
    
    // Contact info
    private ContactInfoDTO contactInfo;
    
    // Metadata
    private ArticleMetadataDTO metadata;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    
    /**
     * Inner DTO for content structure
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContentStructureDTO {
        private String type; // "structured", "markdown", "html"
        private List<ContentBlockDTO> blocks;
    }
}

