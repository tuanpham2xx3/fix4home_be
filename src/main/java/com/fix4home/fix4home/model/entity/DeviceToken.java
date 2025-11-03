package com.fix4home.fix4home.model.entity;

import com.fix4home.fix4home.model.enums.DevicePlatform;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_tokens", indexes = {
    @Index(name = "idx_device_tokens_user_id", columnList = "user_id"),
    @Index(name = "idx_device_tokens_token", columnList = "token"),
    @Index(name = "idx_device_tokens_platform", columnList = "platform"),
    @Index(name = "idx_device_tokens_is_active", columnList = "is_active"),
    @Index(name = "idx_device_tokens_last_used", columnList = "last_used_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "token", nullable = false, length = 512, unique = true)
    private String token;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private DevicePlatform platform;
    
    @Column(name = "device_id", length = 200)
    private String deviceId;
    
    @Column(name = "device_name", length = 100)
    private String deviceName;
    
    @Column(name = "app_version", length = 20)
    private String appVersion;
    
    @Column(name = "os_version", length = 50)
    private String osVersion;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "notifications_enabled")
    @Builder.Default
    private Boolean notificationsEnabled = true;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "failed_attempts")
    @Builder.Default
    private Integer failedAttempts = 0;
    
    @Column(name = "last_failure_at")
    private LocalDateTime lastFailureAt;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Business methods
    public void markAsUsed() {
        this.lastUsedAt = LocalDateTime.now();
        this.failedAttempts = 0;
        this.lastFailureAt = null;
    }
    
    public void markAsFailed() {
        this.failedAttempts = (this.failedAttempts != null ? this.failedAttempts : 0) + 1;
        this.lastFailureAt = LocalDateTime.now();
        
        // Deactivate token after 5 consecutive failures
        if (this.failedAttempts >= 5) {
            this.isActive = false;
        }
    }
    
    public boolean shouldRetry() {
        return this.isActive && this.failedAttempts < 3;
    }
    
    public boolean isStale() {
        if (lastUsedAt == null) {
            return createdAt.isBefore(LocalDateTime.now().minusDays(30));
        }
        return lastUsedAt.isBefore(LocalDateTime.now().minusDays(30));
    }
    
    public boolean isFCM() {
        return platform == DevicePlatform.ANDROID || platform == DevicePlatform.WEB;
    }
    
    public boolean isAPNS() {
        return platform == DevicePlatform.IOS;
    }
}
