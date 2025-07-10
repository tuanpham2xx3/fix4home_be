package com.fix4home.fix4home.model.entity;

import com.fix4home.fix4home.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "technician_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;
    
    @Column(name = "full_name", length = 100)
    private String fullName;
    
    @Column(columnDefinition = "TEXT")
    private String skills;
    
    @Column(name = "experience")
    private String experience;
    
    @Column(columnDefinition = "FLOAT DEFAULT 0")
    @Builder.Default
    private Float rating = 0.0f;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
    
    // Online/Offline Status and Location fields
    @Column(name = "is_online", nullable = false)
    @Builder.Default
    private Boolean isOnline = false;
    
    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;
    
    @Column(name = "current_latitude")
    private Double currentLatitude;
    
    @Column(name = "current_longitude")
    private Double currentLongitude;
    
    @Column(name = "current_address", length = 500)
    private String currentAddress;
    
    @Column(name = "working_radius")
    @Builder.Default
    private Integer workingRadius = 10; // Default 10km working radius
} 