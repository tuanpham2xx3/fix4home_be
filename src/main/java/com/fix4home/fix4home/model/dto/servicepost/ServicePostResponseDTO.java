package com.fix4home.fix4home.model.dto.servicepost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePostResponseDTO {
    
    private Long id;
    private Long servicePostId;
    private Long technicianId;
    private String technicianName;
    private String technicianAvatar;
    private Double technicianRating;
    private String message;
    private BigDecimal quotedPrice;
    private Integer estimatedDuration; // in minutes
    private LocalDateTime proposedTime;
    private Boolean isSelected;
    private LocalDateTime createdAt;
} 