package com.fix4home.fix4home.model.dto.admin;

import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserManagementDTO {
    
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    
    // Profile information
    private String fullName;
    private String profileStatus;
    private Float rating;
    
    // Activity information
    private long totalServiceRequests;
    private long completedServiceRequests;
    private BigDecimal totalEarnings;
    
    // Additional info for technicians
    private long totalSkills;
    private boolean isApproved;
    
    // Additional info for customers
    private long totalAddresses;
    private String preferredCity;
} 