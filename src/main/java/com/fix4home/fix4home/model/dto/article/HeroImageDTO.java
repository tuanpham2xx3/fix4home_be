package com.fix4home.fix4home.model.dto.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for hero image metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeroImageDTO {
    
    private String url;
    private String alt;
    private String caption;
}

