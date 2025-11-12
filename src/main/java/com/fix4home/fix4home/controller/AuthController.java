package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.config.RateLimitConfig;
import com.fix4home.fix4home.exception.TokenRefreshException;
import com.fix4home.fix4home.model.dto.auth.AuthResponse;
import com.fix4home.fix4home.model.dto.auth.ChangePasswordRequest;
import com.fix4home.fix4home.model.dto.auth.ForgotPasswordRequest;
import com.fix4home.fix4home.model.dto.auth.LoginRequest;
import com.fix4home.fix4home.model.dto.auth.RefreshTokenResponse;
import com.fix4home.fix4home.model.dto.auth.RegisterRequest;
import com.fix4home.fix4home.model.dto.auth.ResetPasswordRequest;
import com.fix4home.fix4home.model.dto.auth.SendVerificationCodeRequest;
import com.fix4home.fix4home.model.dto.auth.TokenInfoDTO;
import com.fix4home.fix4home.model.dto.auth.VerifyEmailRequest;
import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.entity.ActivationToken;
import com.fix4home.fix4home.model.entity.RefreshToken;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.UserStatus;
import com.fix4home.fix4home.repository.ActivationTokenRepository;
import com.fix4home.fix4home.repository.UserRepository;
import java.time.LocalDateTime;
import com.fix4home.fix4home.security.CustomUserDetails;
import com.fix4home.fix4home.security.JwtTokenProvider;
import com.fix4home.fix4home.security.SecurityConstants;
import com.fix4home.fix4home.service.AuthService;
import com.fix4home.fix4home.service.EmailVerificationService;
import com.fix4home.fix4home.service.ActivationTokenService;
import com.fix4home.fix4home.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;
    private final ActivationTokenService activationTokenService;
    private final RateLimitConfig rateLimitConfig;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivationTokenRepository activationTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        
        // Auto-detect client type based on X-Device-Id header
        // If deviceId is provided: Mobile client → return refresh token in body, no cookie
        // If deviceId is not provided: Web client → set cookie (backward compatible)
        boolean isMobileClient = deviceId != null && !deviceId.trim().isEmpty();
        
        if (isMobileClient) {
            // Mobile client: Create refresh token and add to response body
            try {
                RefreshToken refreshToken = createRefreshTokenForUser(authResponse.getUserId(), deviceId);
                authResponse.setRefreshToken(refreshToken.getToken());
                log.info("Registration successful for mobile client - user: {}, device: {}", 
                        authResponse.getUserId(), deviceId);
            } catch (Exception e) {
                log.error("Error creating refresh token for mobile client - user: {}, device: {}", 
                        authResponse.getUserId(), deviceId, e);
                // Still return success response with access token
                return ResponseEntity.ok(
                    ApiResponse.success("User registered successfully but failed to create refresh token", authResponse));
            }
        } else {
            // Web client: No deviceId provided
            // Note: Refresh token creation requires deviceId (enforced by RefreshTokenService)
            // Web clients that need refresh token should provide X-Device-Id header
            // This maintains backward compatibility - web clients without deviceId still get access token
            log.info("Registration successful for web client (no deviceId) - user: {}", authResponse.getUserId());
        }
        
        return ResponseEntity.ok(
                ApiResponse.success("User registered successfully", authResponse));
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody LoginRequest request,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            HttpServletResponse response) {
        // Validation is done in service layer to support email, username, or usernameOrEmail fields
        AuthResponse authResponse = authService.login(request);
        
        // Auto-detect client type based on X-Device-Id header
        // If deviceId is provided: Mobile client → return refresh token in body, no cookie
        // If deviceId is not provided: Web client → set cookie (backward compatible)
        boolean isMobileClient = deviceId != null && !deviceId.trim().isEmpty();
        
        if (isMobileClient) {
            // Mobile client: Create refresh token and add to response body
            try {
                RefreshToken refreshToken = createRefreshTokenForUser(authResponse.getUserId(), deviceId);
                authResponse.setRefreshToken(refreshToken.getToken());
                log.info("Login successful for mobile client - user: {}, device: {}", 
                        authResponse.getUserId(), deviceId);
            } catch (Exception e) {
                log.error("Error creating refresh token for mobile client - user: {}, device: {}", 
                        authResponse.getUserId(), deviceId, e);
                // Still return success response with access token
                return ResponseEntity.ok(
                    ApiResponse.success("Login successful but failed to create refresh token", authResponse));
            }
        } else {
            // Web client: No deviceId provided
            // Note: Refresh token creation requires deviceId (enforced by RefreshTokenService)
            // Web clients that need refresh token should provide X-Device-Id header
            // This maintains backward compatibility - web clients without deviceId still get access token
            log.info("Login successful for web client (no deviceId) - user: {}", authResponse.getUserId());
        }
        
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie,
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshTokenHeader,
            @RequestBody(required = false) Map<String, String> requestBody,
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

        // Priority: Header > Body > Cookie (for backward compatibility with web clients)
        String refreshToken = refreshTokenHeader != null && !refreshTokenHeader.trim().isEmpty() 
                ? refreshTokenHeader 
                : (requestBody != null && requestBody.containsKey("refreshToken") 
                        ? requestBody.get("refreshToken") 
                        : refreshTokenCookie);
        
        String tokenSource = refreshTokenHeader != null && !refreshTokenHeader.trim().isEmpty() 
                ? "header" 
                : (requestBody != null && requestBody.containsKey("refreshToken") 
                        ? "body" 
                        : "cookie");
        
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            log.warn("Refresh token required but not provided from any source (header/body/cookie)");
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
                        
                        // Log successful refresh with token source
                        log.info("Access token refreshed successfully for user: {}, device: {}, token source: {}", 
                                token.getUser().getId(), token.getDeviceId(), tokenSource);
                        
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
                        log.warn("Invalid refresh token attempt from source: {}", tokenSource);
                        return ResponseEntity.badRequest()
                                .body(ApiResponse.error("Invalid refresh token"));
                    });
        } catch (TokenRefreshException e) {
            log.error("Error refreshing token from source: {} - {}", tokenSource, e.getMessage());
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

    @PostMapping("/change-password")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("POST /api/v1/auth/change-password - Change password");
        authService.changePassword(request);
        return ResponseEntity.ok(
                ApiResponse.<Void>success("Password changed successfully", null));
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

        // Send verification for registration verification
        boolean emailSent = emailVerificationService.sendVerification(
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
    public ResponseEntity<ApiResponse<ActivationTokenService.PasswordResetTokenResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        
        // Check if user exists with this email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null) {
            // For security, we don't reveal if email exists or not
            ActivationTokenService.PasswordResetTokenResponse response = 
                ActivationTokenService.PasswordResetTokenResponse.builder()
                    .success(true)
                    .message("If the email exists, a password reset email with temporary password has been sent")
                    .build();
            return ResponseEntity.ok(ApiResponse.success("Password reset email sent", response));
        }

        try {
            // Generate and send password reset token with temp password
            ActivationTokenService.PasswordResetTokenResponse response = 
                activationTokenService.generatePasswordResetToken(user);

            log.info("Password reset email with temporary password sent to: {}", request.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.success("Password reset email sent successfully", response));
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", request.getEmail(), e);
            
            if (e.getMessage().contains("60 seconds") || e.getMessage().contains("Maximum resend")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(ApiResponse.error(e.getMessage()));
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to send password reset email. Please try again later."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        try {
            // Verify activation token
            ActivationTokenService.ActivationTokenData tokenData = 
                activationTokenService.verifyActivationToken(request.getCode()); // Using code field for token

            // Verify this is a password reset token
            if (!"password_reset".equals(tokenData.getAction())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid token type"));
            }

            User user = tokenData.getUser();

            // Update user's password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // Revoke all existing refresh tokens for security
            refreshTokenService.deleteByUserId(user.getId());

            log.info("Password reset successfully via activation token for user: {}", user.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.<Void>success("Password reset successfully", null));
        } catch (Exception e) {
            log.error("Password reset failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired reset token"));
        }
    }

    /**
     * Helper method to create refresh token for a user and device.
     * This method is used by both mobile (returns token in body) and web (sets cookie) flows.
     * 
     * @param userId The user ID
     * @param deviceId The device ID (required)
     * @return The created RefreshToken entity
     */
    private RefreshToken createRefreshTokenForUser(Long userId, String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Device ID is required to create refresh token");
        }
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId, deviceId);
        log.info("Created refresh token for user: {}, device: {}", userId, deviceId);
        return refreshToken;
    }

    /**
     * Sets refresh token as HTTP-only cookie for web clients.
     * 
     * @param response HTTP response
     * @param userId The user ID
     * @param deviceId The device ID
     */
    private void setRefreshTokenCookie(HttpServletResponse response, Long userId, String deviceId) {
        RefreshToken refreshToken = createRefreshTokenForUser(userId, deviceId);
        
        Cookie cookie = new Cookie("refresh_token", refreshToken.getToken());
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
        cookie.setPath("/api/v1/auth");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
        
        log.info("Set refresh token cookie for user: {}, device: {} (web client)", userId, deviceId);
    }

    // ===== NEW ACTIVATION ENDPOINTS =====

    @GetMapping("/activate/{token}")
    public ResponseEntity<ApiResponse<Void>> activateAccount(@PathVariable String token) {
        
        try {
            // Verify activation token
            ActivationTokenService.ActivationTokenData tokenData = 
                activationTokenService.verifyActivationToken(token);

            User user = tokenData.getUser();

            // Handle different actions
            if ("registration".equals(tokenData.getAction())) {
                // Registration activation
                if (user.getStatus() == com.fix4home.fix4home.model.enums.UserStatus.PENDING_EMAIL_VERIFICATION) {
                    if (user.getRole() == com.fix4home.fix4home.model.enums.Role.TECHNICIAN) {
                        // Technician needs admin approval after email verification
                        user.setStatus(com.fix4home.fix4home.model.enums.UserStatus.PENDING_APPROVAL);
                        log.info("Email verified for technician via activation link, status set to PENDING_APPROVAL: {}", user.getEmail());
                    } else {
                        // Customer and other roles are activated immediately after email verification
                        user.setStatus(com.fix4home.fix4home.model.enums.UserStatus.ACTIVE);
                        log.info("Email verified and user activated via activation link: {}", user.getEmail());
                    }
                }
                userRepository.save(user);
                return ResponseEntity.ok(
                        ApiResponse.<Void>success("Account activated successfully", null));
                        
            } else if ("password_reset".equals(tokenData.getAction())) {
                // Password reset activation - temp password already applied by service
                if (tokenData.isTempPasswordApplied()) {
                    // Save user with new temp password and must change flag
                    userRepository.save(user);
                    
                    log.info("Temporary password activated for user: {}", user.getEmail());
                    return ResponseEntity.ok(
                            ApiResponse.<Void>success("Temporary password activated. Please login and change your password.", null));
                } else {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Failed to activate temporary password"));
                }
            }

            return ResponseEntity.ok(
                    ApiResponse.<Void>success("Activation completed successfully", null));
        } catch (Exception e) {
            log.error("Account activation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired activation token"));
        }
    }

    @PostMapping("/verify-activation-token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyActivationToken(
            @RequestBody Map<String, String> request) {
        
        String token = request.get("token");
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token is required"));
        }

        // Verify activation token with microservice
        EmailVerificationService.ActivationData activationData = emailVerificationService.verifyActivationToken(token);
        
        if (activationData == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired activation token"));
        }

        // Find user by email
        User user = userRepository.findByEmail(activationData.getEmail())
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not found"));
        }

        // Process based on action type
        if ("registration".equals(activationData.getAction())) {
            if (user.getStatus() == com.fix4home.fix4home.model.enums.UserStatus.PENDING_EMAIL_VERIFICATION) {
                if (user.getRole() == com.fix4home.fix4home.model.enums.Role.TECHNICIAN) {
                    user.setStatus(com.fix4home.fix4home.model.enums.UserStatus.PENDING_APPROVAL);
                    log.info("Email verified for technician via token, status set to PENDING_APPROVAL: {}", user.getEmail());
                } else {
                    user.setStatus(com.fix4home.fix4home.model.enums.UserStatus.ACTIVE);
                    log.info("Email verified and user activated via token: {}", user.getEmail());
                }
                userRepository.save(user);
            }
        }

        // Return activation data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("email", activationData.getEmail());
        responseData.put("action", activationData.getAction());
        responseData.put("userId", user.getId());
        responseData.put("userStatus", user.getStatus());

        return ResponseEntity.ok(
                ApiResponse.success("Token verified successfully", responseData));
    }

    @PostMapping("/send-activation-link")
    public ResponseEntity<ApiResponse<EmailVerificationService.ActivationResponse>> sendActivationLink(
            @Valid @RequestBody SendVerificationCodeRequest request) {
        
        // Check if user exists with this email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null) {
            // For security, we don't reveal if email exists or not
            EmailVerificationService.ActivationResponse response = EmailVerificationService.ActivationResponse.builder()
                .success(true)
                .message("If the email exists, an activation link has been sent")
                .build();
            return ResponseEntity.ok(ApiResponse.success("Activation link sent", response));
        }

        // Send activation link
        EmailVerificationService.ActivationResponse response = emailVerificationService.sendActivationLink(
                request.getEmail(), 
                "registration", 
                user.getId()
        );

        if (response.isSuccess()) {
            log.info("Activation link sent to email: {}", request.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.success("Activation link sent successfully", response));
        } else {
            // Check if it's a rate limit error
            if (response.getMessage().contains("60 giây") || response.getMessage().contains("giới hạn")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(ApiResponse.error("Rate limit exceeded"));
            }
            
            log.error("Failed to send activation link to email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to send activation link"));
        }
    }

    @PostMapping("/resend-activation-link")
    public ResponseEntity<ApiResponse<EmailVerificationService.ActivationResponse>> resendActivationLink(
            @Valid @RequestBody SendVerificationCodeRequest request) {
        
        // Check if user exists with this email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not found"));
        }

        // Resend activation link
        EmailVerificationService.ActivationResponse response = emailVerificationService.resendActivationLink(
                request.getEmail(), 
                "registration"
        );

        if (response.isSuccess()) {
            log.info("Activation link resent to email: {}", request.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.success("Activation link resent successfully", response));
        } else {
            // Check if it's a rate limit error
            if (response.getMessage().contains("60 giây") || response.getMessage().contains("giới hạn")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(ApiResponse.error("Rate limit exceeded"));
            }
            
            log.error("Failed to resend activation link to email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to resend activation link"));
        }
    }

    @PostMapping("/send-password-reset-link")
    public ResponseEntity<ApiResponse<EmailVerificationService.ActivationResponse>> sendPasswordResetLink(
            @Valid @RequestBody ForgotPasswordRequest request) {
        
        // Check if user exists with this email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null) {
            // For security, we don't reveal if email exists or not
            EmailVerificationService.ActivationResponse response = EmailVerificationService.ActivationResponse.builder()
                .success(true)
                .message("If the email exists, a password reset link has been sent")
                .build();
            return ResponseEntity.ok(ApiResponse.success("Password reset link sent", response));
        }

        // Send password reset link
        EmailVerificationService.ActivationResponse response = emailVerificationService.sendActivationLink(
                request.getEmail(), 
                "password_reset", 
                user.getId()
        );

        if (response.isSuccess()) {
            log.info("Password reset link sent to email: {}", request.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.success("Password reset link sent successfully", response));
        } else {
            // Check if it's a rate limit error
            if (response.getMessage().contains("60 giây") || response.getMessage().contains("giới hạn")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(ApiResponse.error("Rate limit exceeded"));
            }
            
            log.error("Failed to send password reset link to email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to send password reset link"));
        }
    }

    @PostMapping("/reset-password-with-token")
    public ResponseEntity<ApiResponse<Void>> resetPasswordWithToken(
            @RequestBody Map<String, String> request) {
        
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token is required"));
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("New password is required"));
        }

        // Verify activation token with microservice
        EmailVerificationService.ActivationData activationData = emailVerificationService.verifyActivationToken(token);
        
        if (activationData == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired reset token"));
        }

        // Verify this is a password reset token
        if (!"password_reset".equals(activationData.getAction())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid token type"));
        }

        // Find user by email
        User user = userRepository.findByEmail(activationData.getEmail())
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not found"));
        }

        // Update user's password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Revoke all existing refresh tokens for security
        refreshTokenService.deleteByUserId(user.getId());

        log.info("Password reset successfully via token for user: {}", user.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.<Void>success("Password reset successfully", null));
    }

    // ===== TOKEN STATUS ENDPOINTS =====

    @GetMapping("/check-token/{token}")
    public ResponseEntity<ApiResponse<ActivationTokenService.ActivationTokenInfo>> checkTokenStatus(
            @PathVariable String token) {
        
        Optional<ActivationTokenService.ActivationTokenInfo> tokenInfo = 
            activationTokenService.getTokenInfo(token);
        
        if (tokenInfo.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token not found"));
        }

        return ResponseEntity.ok(
                ApiResponse.success("Token information retrieved", tokenInfo.get()));
    }

    @PostMapping("/resend-password-reset")
    public ResponseEntity<ApiResponse<ActivationTokenService.ActivationTokenResponse>> resendPasswordReset(
            @Valid @RequestBody ForgotPasswordRequest request) {
        
        // Check if user exists with this email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not found"));
        }

        try {
            // Resend password reset token
            ActivationTokenService.ActivationTokenResponse response = 
                activationTokenService.resendActivationToken(request.getEmail(), "password_reset");

            log.info("Password reset link resent to email: {}", request.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.success("Password reset link resent successfully", response));
        } catch (Exception e) {
            log.error("Failed to resend password reset link to email: {}", request.getEmail(), e);
            
            if (e.getMessage().contains("60 seconds") || e.getMessage().contains("Maximum resend")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(ApiResponse.error(e.getMessage()));
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to resend password reset link"));
        }
    }

    /**
     * Check activation status by email (for frontend to check without token)
     * @param email user email
     * @return activation status information
     */
    @GetMapping("/check-activation-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkActivationStatus(
            @RequestParam String email) {
        
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not found"));
        }
        
        // Check if there's an active token
        LocalDateTime now = LocalDateTime.now();
        Optional<ActivationToken> activeToken = activationTokenRepository
                .findActiveTokenByEmailAndAction(email, "registration", now);
        
        Map<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("email", user.getEmail());
        statusInfo.put("userId", user.getId());
        statusInfo.put("userStatus", user.getStatus().toString());
        statusInfo.put("isActivated", user.getStatus() != UserStatus.PENDING_EMAIL_VERIFICATION);
        statusInfo.put("hasActiveToken", activeToken.isPresent());
        
        if (activeToken.isPresent()) {
            ActivationToken token = activeToken.get();
            statusInfo.put("tokenExpiresAt", token.getExpiresAt().toString());
            statusInfo.put("canResend", token.canResend());
            if (token.getLastSentAt() != null) {
                statusInfo.put("lastSentAt", token.getLastSentAt().toString());
            }
        }
        
        return ResponseEntity.ok(
                ApiResponse.success("Activation status retrieved", statusInfo));
    }
}