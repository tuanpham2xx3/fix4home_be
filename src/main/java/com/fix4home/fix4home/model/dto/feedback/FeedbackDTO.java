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
public class FeedbackDTO {
    
    private Long id;
    private Integer rating;
    private String comment;
    private String reply;
    private LocalDateTime createdAt;
    
    // Service Request details
    private Long serviceRequestId;
    private String serviceRequestDescription;
    private String serviceName;
    
    // Customer details
    private Long customerId;
    private String customerName;
    private String customerEmail;
    
    // Technician details
    private Long technicianId;
    private String technicianName;
    private String technicianEmail;
    
    // UI fields
    private String ratingDisplay;
    private String timeAgo;
    private boolean canReply;
    private boolean canEdit;
    private boolean hasReply;
    
    // Rating breakdown
    private String ratingStars;
    private String ratingText;
    
    // Service completion info
    private LocalDateTime serviceCompletedAt;
    private String serviceStatus;
} 