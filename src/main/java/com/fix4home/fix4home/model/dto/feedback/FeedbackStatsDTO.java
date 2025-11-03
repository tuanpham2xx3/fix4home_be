package com.fix4home.fix4home.model.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackStatsDTO {
    
    // Overall statistics
    private long totalFeedbacks;
    private double averageRating;
    private long repliedFeedbacks;
    private long unrepliedFeedbacks;
    
    // Rating breakdown
    private long fiveStarCount;
    private long fourStarCount;
    private long threeStarCount;
    private long twoStarCount;
    private long oneStarCount;
    
    // Percentage breakdown
    private double fiveStarPercentage;
    private double fourStarPercentage;
    private double threeStarPercentage;
    private double twoStarPercentage;
    private double oneStarPercentage;
    
    // Performance metrics
    private double replyRate;
    private double satisfactionRate; // >= 4 stars
    private double excellentRate; // 5 stars
    
    // Time-based statistics
    private Map<String, Long> feedbacksByMonth;
    private Map<String, Double> averageRatingByMonth;
    
    // Top performers (for admin)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopTechnicianStats {
        private Long technicianId;
        private String technicianName;
        private Double averageRating;
        private Long totalFeedbacks;
        private Double excellentRate;
    }
} 