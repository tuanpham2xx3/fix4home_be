package com.fix4home.fix4home.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "activation_tokens", indexes = {
    @Index(name = "idx_activation_token", columnList = "token"),
    @Index(name = "idx_activation_email_action", columnList = "email, action"),
    @Index(name = "idx_activation_expires_at", columnList = "expires_at"),
    @Index(name = "idx_activation_user_id", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 36)
    private String token; // UUID token

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "action", nullable = false, length = 50)
    private String action; // "registration", "password_reset", "email_verification"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = false;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "send_count", nullable = false)
    @Builder.Default
    private Integer sendCount = 1;

    @Column(name = "last_sent_at", nullable = false)
    private LocalDateTime lastSentAt;

    @Column(name = "temp_password", length = 500)
    private String tempPassword; // Encrypted temporary password for password reset

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return used != null && used;
    }

    public boolean isValid() {
        return !isExpired() && !isUsed();
    }

    public boolean canResend() {
        return sendCount < 3 && 
               (lastSentAt == null || LocalDateTime.now().isAfter(lastSentAt.plusSeconds(60)));
    }

    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    public void incrementSendCount() {
        this.sendCount++;
        this.lastSentAt = LocalDateTime.now();
    }
}
