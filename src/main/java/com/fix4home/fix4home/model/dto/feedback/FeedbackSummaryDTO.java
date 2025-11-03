package com.fix4home.fix4home.model.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackSummaryDTO {
    
    private Long id;
    private Integer rating;
    private String comment;
    private boolean hasReply;
    private LocalDateTime createdAt;
    
    // Customer info
    private Long customerId;
    private String customerName;
    
    // Technician info
    private Long technicianId;
    private String technicianName;
    
    // Service info
    private Long serviceRequestId;
    private String serviceName;
    
    // UI fields
    private String ratingStars;
    private String timeAgo;
    private String commentPreview; // Truncated comment
} 