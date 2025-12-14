package com.fix4home.fix4home.model.dto.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for article sections
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionDTO {
    
    private String title;
    private String content;
    private List<String> bulletPoints;
}

