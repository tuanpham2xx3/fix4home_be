package com.fix4home.fix4home.model.dto.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for article metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleMetadataDTO {
    
    private String author;
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private List<String> tags;
    private String category;
}

