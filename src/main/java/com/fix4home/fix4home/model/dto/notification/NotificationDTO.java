package com.fix4home.fix4home.model.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    
    // Additional fields for convenience
    private String timeAgo;
    private String userFullName;
    private String userEmail;
    
    // For grouping/filtering
    private String category;
    private String priority;
} 