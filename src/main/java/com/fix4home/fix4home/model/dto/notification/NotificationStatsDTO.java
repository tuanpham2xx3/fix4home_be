package com.fix4home.fix4home.model.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationStatsDTO {
    
    // User-specific stats
    private Long userId;
    private long totalNotifications;
    private long unreadNotifications;
    private long readNotifications;
    private double readPercentage;
    
    // Recent activity
    private long notificationsToday;
    private long notificationsThisWeek;
    private long notificationsThisMonth;
    
    // Last activity
    private LocalDateTime lastNotificationAt;
    private LocalDateTime lastReadAt;
    
    // Category breakdown
    private Map<String, Long> notificationsByCategory;
    private Map<String, Long> unreadByCategory;
    
    // Priority breakdown  
    private Map<String, Long> notificationsByPriority;
    
    // Admin stats (system-wide)
    private SystemNotificationStats systemStats;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SystemNotificationStats {
        private long totalSystemNotifications;
        private long totalUsers;
        private long activeUsers;
        private double averageNotificationsPerUser;
        private double systemReadPercentage;
        private LocalDateTime generatedAt;
        
        // Most active periods
        private Map<String, Long> notificationsByHour;
        private Map<String, Long> notificationsByDay;
        
        // Popular categories
        private Map<String, Long> topCategories;
        private Map<String, Long> topPriorities;
    }
} 