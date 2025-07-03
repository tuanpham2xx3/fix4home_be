package com.fix4home.fix4home.model.entity;

import com.fix4home.fix4home.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

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
} 