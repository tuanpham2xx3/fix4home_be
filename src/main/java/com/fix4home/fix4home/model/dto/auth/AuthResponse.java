package com.fix4home.fix4home.model.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn; // in seconds
    
    // Refresh token (only populated for mobile clients with X-Device-Id header)
    private String refreshToken;
    
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
    private String avatarUrl;
    
    // For technician
    private String skills;
    private String experience;
    private Float rating;

    public AuthResponse(String accessToken, Long userId) {
        this.accessToken = accessToken;
        this.userId = userId;
    }
} 