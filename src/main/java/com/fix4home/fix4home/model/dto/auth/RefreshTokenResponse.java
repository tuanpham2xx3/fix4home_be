package com.fix4home.fix4home.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // seconds until expiration
    private Long userId;
    
    public RefreshTokenResponse(String accessToken, Long userId, Long expiresIn) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.expiresIn = expiresIn;
        this.tokenType = "Bearer";
    }
} 