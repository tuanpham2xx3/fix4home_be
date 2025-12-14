package com.fix4home.fix4home.model.dto.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for paginated article list
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleListResponseDTO {
    
    private List<ArticleDTO> articles;
    private Long total;
    private Integer page;
    private Integer limit;
    private Integer totalPages;
}

