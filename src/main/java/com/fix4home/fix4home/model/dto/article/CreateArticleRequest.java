package com.fix4home.fix4home.model.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a new article
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateArticleRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;
    
    @Size(max = 1000, message = "Short description must not exceed 1000 characters")
    private String shortDescription;
    
    @NotBlank(message = "Slug is required")
    @Size(max = 500, message = "Slug must not exceed 500 characters")
    private String slug;
    
    @NotNull(message = "Content is required")
    private ContentStructureDTO content;
    
    private HeroImageDTO heroImage;
    
    @Size(max = 500, message = "Meta description must not exceed 500 characters")
    private String metaDescription;
    
    @Size(max = 500, message = "Meta keywords must not exceed 500 characters")
    private String metaKeywords;
    
    private List<SectionDTO> sections;
    
    private ContactInfoDTO contactInfo;
    
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

