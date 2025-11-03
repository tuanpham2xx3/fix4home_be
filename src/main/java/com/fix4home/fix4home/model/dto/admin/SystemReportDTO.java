package com.fix4home.fix4home.model.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemReportDTO {
    
    private String reportType; // DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime generatedAt;
    
    // User activity
    private long newUsersCount;
    private long activeUsersCount;
    private long totalLoginCount;
    
    // Service requests
    private long newRequestsCount;
    private long completedRequestsCount;
    private long cancelledRequestsCount;
    
    // Financial data
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;
    private BigDecimal averageRequestValue;
    
    // Performance metrics
    private double avgCompletionTime;
    private double customerSatisfactionScore;
    private List<TopPerformerDTO> topTechnicians;
    private List<PopularServiceDTO> popularServices;
    private List<ActivityTrendDTO> activityTrends;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopPerformerDTO {
        private Long technicianId;
        private String technicianName;
        private long completedJobs;
        private Float averageRating;
        private BigDecimal totalEarnings;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PopularServiceDTO {
        private Long serviceId;
        private String serviceName;
        private long requestCount;
        private BigDecimal totalRevenue;
        private double averageRating;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActivityTrendDTO {
        private LocalDate date;
        private long requestCount;
        private long userCount;
        private BigDecimal revenue;
    }
} 