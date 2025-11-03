package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.config.RateLimitConfig;
import com.fix4home.fix4home.exception.TokenRefreshException;
import com.fix4home.fix4home.model.dto.auth.AuthResponse;
import com.fix4home.fix4home.model.dto.auth.ForgotPasswordRequest;
import com.fix4home.fix4home.model.dto.auth.LoginRequest;
import com.fix4home.fix4home.model.dto.auth.RefreshTokenResponse;
import com.fix4home.fix4home.model.dto.auth.RegisterRequest;
import com.fix4home.fix4home.model.dto.auth.ResetPasswordRequest;
import com.fix4home.fix4home.model.dto.auth.SendVerificationCodeRequest;
import com.fix4home.fix4home.model.dto.auth.TokenInfoDTO;
import com.fix4home.fix4home.model.dto.auth.VerifyEmailRequest;
import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.entity.RefreshToken;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.repository.UserRepository;
import com.fix4home.fix4home.security.CustomUserDetails;
import com.fix4home.fix4home.security.JwtTokenProvider;
import com.fix4home.fix4home.service.AuthService;
import com.fix4home.fix4home.service.EmailVerificationService;
import com.fix4home.fix4home.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final EmailVerificationService emailVerificationService;
    private final RateLimitConfig rateLimitConfig;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
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
                    .map(token -> {
                        // Generate new access token only
                        String accessToken = authService.generateAccessToken(token.getUser());
                        
                        // Update last used time of the refresh token
                        refreshTokenService.updateLastUsedTime(token);
                        
                        // Log successful refresh
                        log.info("Access token refreshed successfully for user: {}, device: {}", 
                                token.getUser().getId(), token.getDeviceId());
                        
                        // Calculate expires in seconds (15 minutes)
                        Long expiresIn = 900L; // 15 * 60 seconds
                        
                        RefreshTokenResponse refreshResponse = new RefreshTokenResponse(
                                accessToken, 
                                token.getUser().getId(), 
                                expiresIn
                        );
                        
                        return ResponseEntity.ok(
                                ApiResponse.success("Token refreshed successfully", refreshResponse));
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
            @RequestHeader(value = "X-Device-Id", required = true) String deviceId,
            HttpServletResponse response) {
        
        if (refreshToken == null) {
            return ResponseEntity.ok(
                    ApiResponse.<Void>success("No active session", null));
        }

        // Get current refresh token
        return refreshTokenService.findByToken(refreshToken)
                .map(token -> {
                    // Logout specific device only
                    refreshTokenService.revokeTokenByDeviceId(token.getUser().getId(), deviceId);
                    log.info("Logged out device: {} for user: {}", deviceId, token.getUser().getId());

                    // Clear refresh token cookie
                    Cookie cookie = new Cookie("refresh_token", "");
                    cookie.setMaxAge(0);
                    cookie.setPath("/api/v1/auth");
                    cookie.setHttpOnly(true);
                    cookie.setSecure(true);
                    response.addCookie(cookie);

                    return ResponseEntity.ok(
                            ApiResponse.<Void>success("Logged out successfully", null));
                })
                .orElse(ResponseEntity.ok(
                        ApiResponse.<Void>success("No active session", null)));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAllDevices(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        
        if (refreshToken == null) {
            return ResponseEntity.ok(
                    ApiResponse.<Void>success("No active session", null));
        }

        // Get current refresh token to identify user
        return refreshTokenService.findByToken(refreshToken)
                .map(token -> {
                    // Logout all devices for this user
                    refreshTokenService.deleteByUserId(token.getUser().getId());
                    log.info("Logged out all devices for user: {}", token.getUser().getId());

                    // Clear refresh token cookie
                    Cookie cookie = new Cookie("refresh_token", "");
                    cookie.setMaxAge(0);
                    cookie.setPath("/api/v1/auth");
                    cookie.setHttpOnly(true);
                    cookie.setSecure(true);
                    response.addCookie(cookie);

                    return ResponseEntity.ok(
                            ApiResponse.<Void>success("Logged out from all devices successfully", null));
                })
                .orElse(ResponseEntity.ok(
                        ApiResponse.<Void>success("No active session", null)));
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

    @PostMapping("/send-verification-code")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
            @Valid @RequestBody SendVerificationCodeRequest request) {
        
        // Check if user exists with this email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null) {
            // For security, we don't reveal if email exists or not
            return ResponseEntity.ok(
                    ApiResponse.<Void>success("If the email exists, a verification code has been sent", null));
        }

        // Send verification code for registration verification
        boolean emailSent = emailVerificationService.sendVerificationCode(
                request.getEmail(), 
                "email_verification", 
                user.getId()
        );

        if (emailSent) {
            log.info("Verification code sent to email: {}", request.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.<Void>success("Verification code sent successfully", null));
        } else {
            log.error("Failed to send verification code to email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to send verification code. Please try again later."));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid email or verification code"));
        }

        // Verify the code with microservice
        boolean isValidCode = emailVerificationService.verifyCode(request.getEmail(), request.getCode());

        if (!isValidCode) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired verification code"));
        }

        // Update user status after email verification
        if (user.getStatus() == com.fix4home.fix4home.model.enums.UserStatus.PENDING_EMAIL_VERIFICATION) {
            if (user.getRole() == com.fix4home.fix4home.model.enums.Role.TECHNICIAN) {
                // Technician needs admin approval after email verification
                user.setStatus(com.fix4home.fix4home.model.enums.UserStatus.PENDING_APPROVAL);
                log.info("Email verified for technician, status set to PENDING_APPROVAL: {}", user.getEmail());
            } else {
                // Customer and other roles are activated immediately after email verification
                user.setStatus(com.fix4home.fix4home.model.enums.UserStatus.ACTIVE);
                log.info("Email verified and user activated: {}", user.getEmail());
            }
            userRepository.save(user);
        }

        return ResponseEntity.ok(
                ApiResponse.<Void>success("Email verified successfully", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        
        // Check if user exists with this email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null) {
            // For security, we don't reveal if email exists or not
            return ResponseEntity.ok(
                    ApiResponse.<Void>success("If the email exists, a password reset code has been sent", null));
        }

        // Send verification code for password reset
        boolean emailSent = emailVerificationService.sendVerificationCode(
                request.getEmail(), 
                "forgot_password", 
                user.getId()
        );

        if (emailSent) {
            log.info("Password reset code sent to email: {}", request.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.<Void>success("Password reset code sent successfully", null));
        } else {
            log.error("Failed to send password reset code to email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to send password reset code. Please try again later."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid email or verification code"));
        }

        // Verify the code with microservice
        boolean isValidCode = emailVerificationService.verifyCode(request.getEmail(), request.getCode());

        if (!isValidCode) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired verification code"));
        }

        // Update user's password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all existing refresh tokens for security
        refreshTokenService.deleteByUserId(user.getId());

        log.info("Password reset successfully for user: {}", user.getEmail());
        return ResponseEntity.ok(
                ApiResponse.<Void>success("Password reset successfully", null));
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