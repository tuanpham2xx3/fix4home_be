package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.config.RateLimitConfig;
import com.fix4home.fix4home.exception.TokenRefreshException;
import com.fix4home.fix4home.model.dto.auth.AuthResponse;
import com.fix4home.fix4home.model.dto.auth.LoginRequest;
import com.fix4home.fix4home.model.dto.auth.RegisterRequest;
import com.fix4home.fix4home.model.dto.auth.TokenInfoDTO;
import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.entity.RefreshToken;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.repository.UserRepository;
import com.fix4home.fix4home.security.CustomUserDetails;
import com.fix4home.fix4home.security.JwtTokenProvider;
import com.fix4home.fix4home.service.AuthService;
import com.fix4home.fix4home.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
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
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

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
    @Transactional
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        
        // Only set refresh token cookie if deviceId is provided
        if (deviceId != null && !deviceId.trim().isEmpty()) {
            try {
                setRefreshTokenCookie(response, authResponse.getUserId(), deviceId);
            } catch (Exception e) {
                log.error("Error creating refresh token", e);
                // Still return success response with access token
                return ResponseEntity.ok(
                    ApiResponse.success("Login successful but failed to create refresh token", authResponse));
            }
        }
        
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
                    .map(oldToken -> {
                        // Generate new access token
                        String accessToken = authService.generateAccessToken(oldToken.getUser());
                        
                        // Rotate refresh token
                        RefreshToken newToken = refreshTokenService.rotateToken(oldToken);
                        
                        // Set new refresh token cookie
                        Cookie cookie = new Cookie("refresh_token", newToken.getToken());
                        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
                        cookie.setPath("/api/v1/auth");
                        cookie.setHttpOnly(true);
                        cookie.setSecure(true);
                        cookie.setAttribute("SameSite", "Strict");
                        response.addCookie(cookie);
                        
                        // Log successful refresh and rotation
                        log.info("Token refreshed and rotated successfully for user: {}, device: {}", 
                                oldToken.getUser().getId(), oldToken.getDeviceId());
                        
                        AuthResponse authResponse = new AuthResponse(accessToken, oldToken.getUser().getId());
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
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            HttpServletResponse response) {
        
        // Clear refresh token from database if exists
        if (refreshToken != null) {
            refreshTokenService.findByToken(refreshToken)
                    .ifPresent(token -> {
                        if (deviceId != null && !deviceId.trim().isEmpty()) {
                            // Logout specific device
                            refreshTokenService.revokeTokenByDeviceId(token.getUser().getId(), deviceId);
                            log.info("Logged out device: {} for user: {}", deviceId, token.getUser().getId());
                        } else {
                            // Logout all devices
                            refreshTokenService.deleteByUserId(token.getUser().getId());
                            log.info("Logged out all devices for user: {}", token.getUser().getId());
                        }
                    });
        }
        
        // Clear refresh token cookie
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setMaxAge(0);
        cookie.setPath("/api/v1/auth");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
        
        String message = deviceId != null ? 
            "Logged out successfully from device" : 
            "Logged out successfully from all devices";
            
        return ResponseEntity.ok(
                ApiResponse.success(message, null));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<TokenInfoDTO>> verifyToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No token provided"));
        }

        String token = authHeader.substring(7);
        boolean isValid = tokenProvider.validateToken(token);

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid token"));
        }

        // Get user information
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Build response
        TokenInfoDTO tokenInfo = TokenInfoDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .expiresIn(tokenProvider.getRemainingTime(token))
                .isValid(true)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Token is valid", tokenInfo));
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
        
        log.info("Set refresh token cookie for user: {}, device: {}", userId, deviceId);
    }
} 