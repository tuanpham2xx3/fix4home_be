package com.fix4home.fix4home.model.dto.complaint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintStatsDTO {
    
    // Overall statistics
    private Long totalComplaints;
    private Long pendingComplaints;
    private Long investigatingComplaints;
    private Long resolvedComplaints;
    private Long rejectedComplaints;
    
    // Time-based statistics
    private Long complaintsThisWeek;
    private Long complaintsThisMonth;
    private Long complaintsToday;
    
    // Resolution statistics
    private Double averageResolutionTimeHours;
    private Long totalResolvedByCurrentAdmin;
    private Double resolutionRate; // Percentage of resolved vs total resolved + rejected
    
    // User statistics
    private Long uniqueComplainants;
    private Long uniqueAccused;
    private Long mostComplaints; // Max complaints by a single user
    
    // Status distribution
    private Double pendingPercentage;
    private Double investigatingPercentage;
    private Double resolvedPercentage;
    private Double rejectedPercentage;
    
    // Timestamps
    private LocalDateTime generatedAt;
    private LocalDateTime lastComplaintAt;
    private LocalDateTime lastResolutionAt;
    
    // Additional metrics
    private Boolean hasUrgentComplaints; // Complaints older than 48 hours
    private Long urgentComplaintsCount;
} 