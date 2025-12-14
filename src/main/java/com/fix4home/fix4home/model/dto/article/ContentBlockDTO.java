package com.fix4home.fix4home.model.dto.article;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for structured content blocks
 * Supports various content types: paragraph, heading, lists, images, links, sections
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentBlockDTO {
    
    /**
     * Block type: paragraph, heading, orderedList, unorderedList, image, link, section
     */
    private String type;
    
    /**
     * Content for paragraph, heading, link text
     */
    private String content;
    
    /**
     * Heading level (1-6) for heading type
     */
    private Integer level;
    
    /**
     * List items for orderedList/unorderedList
     */
    private List<String> items;
    
    /**
     * Image URL for image type
     */
    private String url;
    
    /**
     * Image alt text
     */
    private String alt;
    
    /**
     * Image caption
     */
    private String caption;
    
    /**
     * Link URL for link type
     */
    private String linkUrl;
    
    /**
     * Link text
     */
    private String linkText;
    
    /**
     * Open link in new tab
     */
    private Boolean openInNewTab;
    
    /**
     * Section title for section type
     */
    private String sectionTitle;
    
    /**
     * Nested content blocks for section type
     */
    private List<ContentBlockDTO> sectionContent;
    
    /**
     * Spacing for lists (small, medium, large)
     */
    private String spacing;
    
    /**
     * Additional properties for flexibility
     */
    private Map<String, Object> additionalProperties;
}

