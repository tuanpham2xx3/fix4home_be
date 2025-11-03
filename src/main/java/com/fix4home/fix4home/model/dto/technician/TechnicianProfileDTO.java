package com.fix4home.fix4home.model.dto.technician;

import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianProfileDTO {
    
    // User basic info
    private Long userId;
    private String username;
    private String email;
    private String phoneNumber;
    private Role role;
    private UserStatus userStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Technician profile info
    private Long profileId;
    private String fullName;
    private String skills; // Legacy skills field as text
    private String experience;
    private Float rating;
    private UserStatus status; // Technician approval status
    
    // Skills as list (from TechnicianSkill relationship)
    private List<SkillDTO> skillList;
    
    // Online/Offline Status and Location fields
    private Boolean isOnline;
    private LocalDateTime lastSeenAt;
    private Double currentLatitude;
    private Double currentLongitude;
    private String currentAddress;
    private Integer workingRadius;
    
    // Technician Approval Process fields
    private String verificationDocuments;
    private String rejectionReason;
    private LocalDateTime approvedAt;
    private Long approvedBy;
    private String approvedByUsername;
} 