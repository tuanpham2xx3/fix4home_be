package com.fix4home.fix4home.model.entity;

import com.fix4home.fix4home.model.enums.NotificationStatus;
import com.fix4home.fix4home.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "push_notifications", indexes = {
    @Index(name = "idx_push_notifications_user_id", columnList = "user_id"),
    @Index(name = "idx_push_notifications_device_token_id", columnList = "device_token_id"),
    @Index(name = "idx_push_notifications_status", columnList = "status"),
    @Index(name = "idx_push_notifications_type", columnList = "type"),
    @Index(name = "idx_push_notifications_scheduled_at", columnList = "scheduled_at"),
    @Index(name = "idx_push_notifications_sent_at", columnList = "sent_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_token_id")
    private DeviceToken deviceToken;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "data_payload", columnDefinition = "TEXT")
    private String dataPayload; // JSON string for additional data
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "action_url", length = 500)
    private String actionUrl;
    
    @Column(name = "badge_count")
    private Integer badgeCount;
    
    @Column(name = "sound", length = 50)
    @Builder.Default
    private String sound = "default";
    
    @Column(name = "priority", length = 20)
    @Builder.Default
    private String priority = "normal"; // normal, high
    
    @Column(name = "time_to_live")
    @Builder.Default
    private Integer timeToLive = 86400; // 24 hours in seconds
    
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;
    
    @Column(name = "max_retries")
    @Builder.Default
    private Integer maxRetries = 3;
    
    @Column(name = "external_id", length = 200)
    private String externalId; // FCM message ID, APNS message ID, etc.
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Business methods
    public void markAsSent(String externalId) {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.externalId = externalId;
    }
    
    public void markAsDelivered() {
        this.status = NotificationStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }
    
    public void markAsClicked() {
        this.clickedAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
        this.retryCount++;
    }
    
    public boolean canRetry() {
        return retryCount < maxRetries && status == NotificationStatus.FAILED;
    }
    
    public boolean isScheduled() {
        return scheduledAt != null && scheduledAt.isAfter(LocalDateTime.now());
    }
    
    public boolean isExpired() {
        if (timeToLive == null) return false;
        return createdAt.plusSeconds(timeToLive).isBefore(LocalDateTime.now());
    }
    
    public boolean shouldSendNow() {
        return status == NotificationStatus.PENDING && 
               (scheduledAt == null || scheduledAt.isBefore(LocalDateTime.now())) &&
               !isExpired();
    }
}
