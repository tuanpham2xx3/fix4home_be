package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.BusinessValidationException;
import com.fix4home.fix4home.model.entity.ActivationToken;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.repository.ActivationTokenRepository;
import com.fix4home.fix4home.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivationTokenService extends BaseService {

    private final ActivationTokenRepository activationTokenRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;

    @Value("${email.activation.token.expiry-minutes:30}")
    private int tokenExpiryMinutes;

    @Value("${email.activation.max-sends:3}")
    private int maxSends;

    @Value("${email.activation.resend-cooldown-seconds:60}")
    private int resendCooldownSeconds;

    /**
     * Generate and send activation token for registration
     */
    @Transactional
    public ActivationTokenResponse generateActivationToken(User user, String action) {
        return generateActivationTokenInternal(user, action, null);
    }

    /**
     * Generate and send password reset token with temporary password
     */
    @Transactional
    public PasswordResetTokenResponse generatePasswordResetToken(User user) {
        validateRequired(user, "user");

        String email = user.getEmail();
        LocalDateTime now = LocalDateTime.now();

        // Check rate limiting (max 5 emails per hour)
        long tokensInLastHour = activationTokenRepository.countTokensSentInLastHour(
            email, now.minusHours(1)
        );
        if (tokensInLastHour >= 5) {
            throw new BusinessValidationException("Too many password reset emails sent. Please try again later.");
        }

        // Generate readable temporary password
        String tempPassword = PasswordGenerator.generateReadableTempPassword();
        String encryptedTempPassword = passwordEncoder.encode(tempPassword);

        // Check for existing active token
        Optional<ActivationToken> existingToken = activationTokenRepository
            .findActiveTokenByEmailAndAction(email, "password_reset", now);

        ActivationToken token;
        boolean isNewToken = false;

        if (existingToken.isPresent()) {
            token = existingToken.get();
            
            // Check if can resend (60 seconds cooldown and max 3 sends)
            if (!token.canResend()) {
                if (token.getSendCount() >= maxSends) {
                    throw new BusinessValidationException("Maximum resend limit reached (3 times). Please try again later.");
                } else {
                    LocalDateTime nextAllowedTime = token.getLastSentAt().plusSeconds(resendCooldownSeconds);
                    throw new BusinessValidationException(
                        String.format("Please wait %d seconds before resending.", 
                        java.time.Duration.between(now, nextAllowedTime).getSeconds())
                    );
                }
            }
            
            // Update existing token with new temp password
            token.incrementSendCount();
            token.setTempPassword(encryptedTempPassword); // Update with new temp password
        } else {
            // Create new token
            token = ActivationToken.builder()
                .token(UUID.randomUUID().toString())
                .email(email)
                .action("password_reset")
                .user(user)
                .expiresAt(now.plusMinutes(tokenExpiryMinutes))
                .sendCount(1)
                .lastSentAt(now)
                .tempPassword(encryptedTempPassword)
                .build();
            isNewToken = true;
        }

        // Save token
        token = activationTokenRepository.save(token);

        // Send email with temp password
        try {
            EmailVerificationService.ActivationResponse emailResponse = 
                emailVerificationService.sendPasswordResetWithTempPassword(email, tempPassword, user.getId());
            
            if (!emailResponse.isSuccess()) {
                // If email sending failed and this was a new token, delete it
                if (isNewToken) {
                    activationTokenRepository.delete(token);
                }
                throw new BusinessValidationException("Failed to send password reset email: " + emailResponse.getMessage());
            }
        } catch (Exception e) {
            // If email sending failed and this was a new token, delete it
            if (isNewToken) {
                activationTokenRepository.delete(token);
            }
            throw new BusinessValidationException("Failed to send password reset email: " + e.getMessage());
        }

        log.info("Password reset token generated and sent for user: {}, send count: {}", 
            user.getId(), token.getSendCount());

        return PasswordResetTokenResponse.builder()
            .success(true)
            .message("Password reset email sent successfully")
            .tokenId(token.getId())
            .canResend(token.getSendCount() < maxSends)
            .sendCount(token.getSendCount())
            .maxSends(maxSends)
            .nextResendAt(token.getLastSentAt().plusSeconds(resendCooldownSeconds))
            .expiresAt(token.getExpiresAt())
            .tempPassword(tempPassword) // Plain text for response (if needed for debugging)
            .build();
    }

    /**
     * Internal method to generate activation token
     */
    @Transactional
    private ActivationTokenResponse generateActivationTokenInternal(User user, String action, String encryptedTempPassword) {
        validateRequired(user, "user");
        validateRequired(action, "action");

        String email = user.getEmail();
        LocalDateTime now = LocalDateTime.now();

        // Check rate limiting (max 5 emails per hour)
        long tokensInLastHour = activationTokenRepository.countTokensSentInLastHour(
            email, now.minusHours(1)
        );
        if (tokensInLastHour >= 5) {
            throw new BusinessValidationException("Too many activation emails sent. Please try again later.");
        }

        // Check for existing active token
        Optional<ActivationToken> existingToken = activationTokenRepository
            .findActiveTokenByEmailAndAction(email, action, now);

        ActivationToken token;
        boolean isNewToken = false;

        if (existingToken.isPresent()) {
            token = existingToken.get();
            
            // Check if can resend (60 seconds cooldown and max 3 sends)
            if (!token.canResend()) {
                if (token.getSendCount() >= maxSends) {
                    throw new BusinessValidationException("Maximum resend limit reached (3 times). Please try again later.");
                } else {
                    LocalDateTime nextAllowedTime = token.getLastSentAt().plusSeconds(resendCooldownSeconds);
                    throw new BusinessValidationException(
                        String.format("Please wait %d seconds before resending.", 
                        java.time.Duration.between(now, nextAllowedTime).getSeconds())
                    );
                }
            }
            
            // Update existing token
            token.incrementSendCount();
        } else {
            // Create new token
            token = ActivationToken.builder()
                .token(UUID.randomUUID().toString())
                .email(email)
                .action(action)
                .user(user)
                .expiresAt(now.plusMinutes(tokenExpiryMinutes))
                .sendCount(1)
                .lastSentAt(now)
                .tempPassword(encryptedTempPassword) // Store encrypted temp password if provided
                .build();
            isNewToken = true;
        }

        // Save token
        token = activationTokenRepository.save(token);

        // Send email via microservice
        try {
            EmailVerificationService.ActivationResponse emailResponse;
            
            if ("password_reset".equals(action) && encryptedTempPassword != null) {
                // For password reset, we need to get the plain text password from the response
                // Since we generated it in generatePasswordResetToken method
                // We'll pass it through a different mechanism
                emailResponse = emailVerificationService.sendActivationLink(email, action, user.getId());
            } else {
                // For registration and other actions
                emailResponse = emailVerificationService.sendActivationLink(email, action, user.getId());
            }
            
            if (!emailResponse.isSuccess()) {
                // If email sending failed and this was a new token, delete it
                if (isNewToken) {
                    activationTokenRepository.delete(token);
                }
                throw new BusinessValidationException("Failed to send activation email: " + emailResponse.getMessage());
            }
        } catch (Exception e) {
            // If email sending failed and this was a new token, delete it
            if (isNewToken) {
                activationTokenRepository.delete(token);
            }
            throw new BusinessValidationException("Failed to send activation email: " + e.getMessage());
        }

        log.info("Activation token generated and sent for user: {}, action: {}, send count: {}", 
            user.getId(), action, token.getSendCount());

        return ActivationTokenResponse.builder()
            .success(true)
            .message("Activation link sent successfully")
            .tokenId(token.getId())
            .canResend(token.getSendCount() < maxSends)
            .sendCount(token.getSendCount())
            .maxSends(maxSends)
            .nextResendAt(token.getLastSentAt().plusSeconds(resendCooldownSeconds))
            .expiresAt(token.getExpiresAt())
            .build();
    }

    /**
     * Verify and consume activation token
     */
    @Transactional
    public ActivationTokenData verifyActivationToken(String tokenString) {
        validateRequired(tokenString, "token");

        ActivationToken token = activationTokenRepository.findByToken(tokenString)
            .orElseThrow(() -> new BusinessValidationException("Invalid activation token"));

        // Check if token is valid
        if (!token.isValid()) {
            if (token.isExpired()) {
                throw new BusinessValidationException("Activation token has expired");
            }
            if (token.isUsed()) {
                throw new BusinessValidationException("Activation token has already been used");
            }
        }

        // Mark token as used
        token.markAsUsed();
        activationTokenRepository.save(token);

        // For security, mark all other tokens for this user and action as used
        activationTokenRepository.markAllTokensAsUsedForUserAndAction(
            token.getUser(), token.getAction(), LocalDateTime.now()
        );

        // If this is a password reset token with temp password, apply it
        if ("password_reset".equals(token.getAction()) && token.getTempPassword() != null) {
            User user = token.getUser();
            user.setPassword(token.getTempPassword()); // Already encrypted
            user.setMustChangePassword(true); // Force user to change password
            // Note: User will be saved by the calling service
        }

        log.info("Activation token verified and consumed for user: {}, action: {}", 
            token.getUser().getId(), token.getAction());

        return ActivationTokenData.builder()
            .tokenId(token.getId())
            .email(token.getEmail())
            .action(token.getAction())
            .user(token.getUser())
            .createdAt(token.getCreatedAt())
            .usedAt(token.getUsedAt())
            .tempPasswordApplied("password_reset".equals(token.getAction()) && token.getTempPassword() != null)
            .build();
    }

    /**
     * Resend activation token (reuse existing token)
     */
    @Transactional
    public ActivationTokenResponse resendActivationToken(String email, String action) {
        validateRequired(email, "email");
        validateRequired(action, "action");

        LocalDateTime now = LocalDateTime.now();

        // Find existing active token
        ActivationToken token = activationTokenRepository
            .findActiveTokenByEmailAndAction(email, action, now)
            .orElseThrow(() -> new BusinessValidationException("No active activation token found"));

        // Check if can resend
        if (!token.canResend()) {
            if (token.getSendCount() >= maxSends) {
                throw new BusinessValidationException("Maximum resend limit reached (3 times)");
            } else {
                LocalDateTime nextAllowedTime = token.getLastSentAt().plusSeconds(resendCooldownSeconds);
                long secondsToWait = java.time.Duration.between(now, nextAllowedTime).getSeconds();
                throw new BusinessValidationException(
                    String.format("Please wait %d seconds before resending", secondsToWait)
                );
            }
        }

        // Update send count
        token.incrementSendCount();
        token = activationTokenRepository.save(token);

        // Resend email via microservice
        try {
            EmailVerificationService.ActivationResponse emailResponse = 
                emailVerificationService.resendActivationLink(email, action);
            
            if (!emailResponse.isSuccess()) {
                throw new BusinessValidationException("Failed to resend activation email: " + emailResponse.getMessage());
            }
        } catch (Exception e) {
            throw new BusinessValidationException("Failed to resend activation email: " + e.getMessage());
        }

        log.info("Activation token resent for email: {}, action: {}, send count: {}", 
            email, action, token.getSendCount());

        return ActivationTokenResponse.builder()
            .success(true)
            .message("Activation link resent successfully")
            .tokenId(token.getId())
            .canResend(token.getSendCount() < maxSends)
            .sendCount(token.getSendCount())
            .maxSends(maxSends)
            .nextResendAt(token.getLastSentAt().plusSeconds(resendCooldownSeconds))
            .expiresAt(token.getExpiresAt())
            .build();
    }

    /**
     * Check if token exists and is valid
     */
    @Transactional(readOnly = true)
    public boolean isTokenValid(String tokenString) {
        return activationTokenRepository.findByToken(tokenString)
            .map(ActivationToken::isValid)
            .orElse(false);
    }

    /**
     * Get token info without consuming it
     */
    @Transactional(readOnly = true)
    public Optional<ActivationTokenInfo> getTokenInfo(String tokenString) {
        return activationTokenRepository.findByToken(tokenString)
            .map(token -> ActivationTokenInfo.builder()
                .tokenId(token.getId())
                .email(token.getEmail())
                .action(token.getAction())
                .userId(token.getUser().getId())
                .expiresAt(token.getExpiresAt())
                .isExpired(token.isExpired())
                .isUsed(token.isUsed())
                .isValid(token.isValid())
                .sendCount(token.getSendCount())
                .createdAt(token.getCreatedAt())
                .build());
    }

    /**
     * Cleanup expired and old used tokens (scheduled task)
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void cleanupTokens() {
        LocalDateTime now = LocalDateTime.now();
        
        // Delete expired tokens
        int expiredCount = activationTokenRepository.deleteExpiredTokens(now);
        
        // Delete used tokens older than 7 days
        int oldUsedCount = activationTokenRepository.deleteOldUsedTokens(now.minusDays(7));
        
        if (expiredCount > 0 || oldUsedCount > 0) {
            log.info("Cleaned up activation tokens: {} expired, {} old used", expiredCount, oldUsedCount);
        }
    }

    // Response DTOs
    @lombok.Data
    @lombok.Builder
    public static class ActivationTokenResponse {
        private boolean success;
        private String message;
        private Long tokenId;
        private boolean canResend;
        private Integer sendCount;
        private Integer maxSends;
        private LocalDateTime nextResendAt;
        private LocalDateTime expiresAt;
    }

    @lombok.Data
    @lombok.Builder
    public static class ActivationTokenData {
        private Long tokenId;
        private String email;
        private String action;
        private User user;
        private LocalDateTime createdAt;
        private LocalDateTime usedAt;
        private boolean tempPasswordApplied;
    }

    @lombok.Data
    @lombok.Builder
    public static class PasswordResetTokenResponse {
        private boolean success;
        private String message;
        private Long tokenId;
        private boolean canResend;
        private Integer sendCount;
        private Integer maxSends;
        private LocalDateTime nextResendAt;
        private LocalDateTime expiresAt;
        private String tempPassword; // Plain text temporary password for email
    }

    @lombok.Data
    @lombok.Builder
    public static class ActivationTokenInfo {
        private Long tokenId;
        private String email;
        private String action;
        private Long userId;
        private LocalDateTime expiresAt;
        private boolean isExpired;
        private boolean isUsed;
        private boolean isValid;
        private Integer sendCount;
        private LocalDateTime createdAt;
    }
}
