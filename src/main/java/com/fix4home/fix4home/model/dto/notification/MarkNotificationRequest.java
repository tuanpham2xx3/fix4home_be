package com.fix4home.fix4home.model.dto.notification;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkNotificationRequest {
    
    @NotNull(message = "Read status is required")
    private Boolean isRead;
    
    // For single notification
    private Long notificationId;
    
    // For bulk operations
    private List<Long> notificationIds;
    
    // For mark all operations
    private Boolean markAll;
    
    // Optional: filter by category when marking all
    private String category;
    private String priority;
    
    // Optional: reason for marking (for audit logs)
    private String reason;
} 