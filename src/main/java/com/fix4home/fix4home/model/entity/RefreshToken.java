package com.fix4home.fix4home.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Entity
@Table(name = "refresh_tokens", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "device_id"}, 
            name = "uk_user_device")
    }
)
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private String deviceId;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        lastUsedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUsedAt = Instant.now();
    }
} 