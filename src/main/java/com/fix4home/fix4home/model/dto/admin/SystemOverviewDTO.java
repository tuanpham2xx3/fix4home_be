package com.fix4home.fix4home.model.dto.admin;

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
public class SystemOverviewDTO {
    
    // User Statistics
    private long totalUsers;
    private long totalCustomers;
    private long totalTechnicians;
    private long activeUsers;
    private long pendingTechnicians;
    private long approvedTechnicians;
    
    // Service Statistics
    private long totalServices;
    private long activeServices;
    private long inactiveServices;
    
    // Service Request Statistics
    private long totalServiceRequests;
    private long pendingRequests;
    private long assignedRequests;
    private long inProgressRequests;
    private long completedRequests;
    private long cancelledRequests;
    
    // Financial Statistics
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal averageRequestValue;
    
    // Performance Metrics
    private double completionRate;
    private double cancellationRate;
    private double customerSatisfactionRate;
    private double averageCompletionTime; // in hours
    
    // System Health
    private int activeConnections;
    private LocalDateTime lastBackupTime;
    private SystemHealth systemHealth;
    private LocalDateTime generatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SystemHealth {
        private String status; // HEALTHY, WARNING, ERROR
        private String message;
        private double cpuUsage;
        private double memoryUsage;
        private double diskUsage;
    }
} 