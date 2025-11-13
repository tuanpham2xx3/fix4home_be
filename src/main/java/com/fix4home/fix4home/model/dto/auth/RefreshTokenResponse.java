package com.fix4home.fix4home.model.dto.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RefreshTokenResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // seconds until expiration
    private Long userId;
    private String username;
    private String email;

    public RefreshTokenResponse(String accessToken,
                                Long userId,
                                String username,
                                String email,
                                Long expiresIn) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.expiresIn = expiresIn;
        this.tokenType = "Bearer";
    }
}