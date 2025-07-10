package com.fix4home.fix4home.model.dto.admin;

import com.fix4home.fix4home.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianApprovalDTO {
    
    // Basic info
    private Long userId;
    private Long profileId;
    private String username;
    private String email;
    private String phoneNumber;
    private String fullName;
    
    // Skills and experience
    private String skills;
    private String experience;
    private Float rating;
    
    // Approval status
    private UserStatus status;
    private String verificationDocuments;
    private String rejectionReason;
    private LocalDateTime approvedAt;
    private Long approvedBy;
    private String approvedByUsername; // Username của admin đã approve
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 