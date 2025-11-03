package com.fix4home.fix4home.model.dto.auth;

import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn; // in seconds
    
    // User info
    private Long userId;
    private String username;
    private String email;
    private String phoneNumber;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
    
    // Profile info
    private String fullName;
    
    // For technician
    private String skills;
    private String experience;
    private Float rating;

    public AuthResponse(String accessToken, Long userId) {
        this.accessToken = accessToken;
        this.userId = userId;
    }
} 