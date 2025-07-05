package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.config.RateLimitConfig;
import com.fix4home.fix4home.exception.TokenRefreshException;
import com.fix4home.fix4home.model.dto.auth.AuthResponse;
import com.fix4home.fix4home.model.dto.auth.LoginRequest;
import com.fix4home.fix4home.model.dto.auth.RegisterRequest;
import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.entity.RefreshToken;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.security.CustomUserDetails;
import com.fix4home.fix4home.service.AuthService;
import com.fix4home.fix4home.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final RateLimitConfig rateLimitConfig;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        
        // Create refresh token and set cookie
        setRefreshTokenCookie(response, authResponse.getUserId(), deviceId);
        
        return ResponseEntity.ok(
                ApiResponse.success("User registered successfully", authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        
        // Create refresh token and set cookie
        setRefreshTokenCookie(response, authResponse.getUserId(), deviceId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress,
            HttpServletResponse response) {
        
        // Get client IP for rate limiting
        String clientIp = ipAddress != null ? ipAddress : "unknown";
        
        // Check rate limit
        if (!rateLimitConfig.resolveBucket(clientIp).tryConsume(1)) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Too many refresh requests. Please try again later."));
        }

        if (refreshToken == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Refresh token is required"));
        }

        try {
            return refreshTokenService.findByToken(refreshToken)
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUser)
                    .map(user -> {
                        String accessToken = authService.generateAccessToken(user);
                        
                        // Create new refresh token (rotation)
                        setRefreshTokenCookie(response, user.getId(), deviceId);
                        
                        // Log successful refresh
                        log.info("Token refreshed successfully for user: {}, device: {}", user.getId(), deviceId);
                        
                        AuthResponse authResponse = new AuthResponse(accessToken, user.getId());
                        return ResponseEntity.ok(
                                ApiResponse.success("Token refreshed successfully", authResponse));
                    })
                    .orElseGet(() -> {
                        log.warn("Invalid refresh token attempt: {}", refreshToken);
                        return ResponseEntity.badRequest()
                                .body(ApiResponse.error("Invalid refresh token"));
                    });
        } catch (TokenRefreshException e) {
            log.error("Error refreshing token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        
        // Clear refresh token from database if exists
        if (refreshToken != null) {
            refreshTokenService.findByToken(refreshToken)
                    .ifPresent(token -> refreshTokenService.deleteByUserId(token.getUser().getId()));
        }
        
        // Clear refresh token cookie
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setMaxAge(0);
        cookie.setPath("/api/v1/auth");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
        
        return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully", null));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, Long userId, String deviceId) {
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId, deviceId);
        
        Cookie cookie = new Cookie("refresh_token", refreshToken.getToken());
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
        cookie.setPath("/api/v1/auth");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }
} 