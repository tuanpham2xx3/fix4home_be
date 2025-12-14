package com.fix4home.fix4home.model.dto.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for contact information in articles
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactInfoDTO {
    
    private String websiteUrl;
    private String bookingPhone;
    private List<String> consultationPhones;
}

