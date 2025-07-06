package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.TokenRefreshException;
import com.fix4home.fix4home.model.entity.RefreshToken;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.repository.RefreshTokenRepository;
import com.fix4home.fix4home.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import jakarta.annotation.PostConstruct;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final MeterRegistry meterRegistry;

    private Counter refreshTokenCreatedCounter;
    private Counter refreshTokenRevokedCounter;
    private Counter refreshTokenExpiredCounter;
    private Counter refreshTokenRotatedCounter;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenDurationMs;

    @PostConstruct
    public void initMetrics() {
        refreshTokenCreatedCounter = Counter.builder("refresh_token_created")
                .description("Number of refresh tokens created")
                .register(meterRegistry);
        
        refreshTokenRevokedCounter = Counter.builder("refresh_token_revoked")
                .description("Number of refresh tokens revoked")
                .register(meterRegistry);
        
        refreshTokenExpiredCounter = Counter.builder("refresh_token_expired")
                .description("Number of refresh tokens expired")
                .register(meterRegistry);

        refreshTokenRotatedCounter = Counter.builder("refresh_token_rotated")
                .description("Number of refresh tokens rotated")
                .register(meterRegistry);
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId, String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new TokenRefreshException("Device ID is required for refresh token creation");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TokenRefreshException("User not found"));

        // Delete existing token for this device if exists
        refreshTokenRepository.findByUserAndDeviceId(user, deviceId)
                .ifPresent(token -> {
                    log.info("Deleting existing token for user: {} and device: {}", userId, deviceId);
                    refreshTokenRepository.delete(token);
                    // Ensure the deletion is flushed before creating new token
                    refreshTokenRepository.flush();
                    refreshTokenRevokedCounter.increment();
                });

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setDeviceId(deviceId);

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        refreshTokenCreatedCounter.increment();
        
        log.info("Created new refresh token for user: {}, device: {}", userId, deviceId);
        return savedToken;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            refreshTokenExpiredCounter.increment();
            log.warn("Refresh token expired for user: {}, device: {}", 
                    token.getUser().getId(), token.getDeviceId());
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired");
        }
        return token;
    }

    @Transactional
    public RefreshToken rotateToken(RefreshToken oldToken) {
        // Verify the old token is still valid
        verifyExpiration(oldToken);
        
        // Create new token
        RefreshToken newToken = new RefreshToken();
        newToken.setUser(oldToken.getUser());
        newToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setDeviceId(oldToken.getDeviceId());
        
        // Save new token
        RefreshToken savedToken = refreshTokenRepository.save(newToken);
        
        // Invalidate old token
        refreshTokenRepository.delete(oldToken);
        
        refreshTokenRotatedCounter.increment();
        log.info("Rotated refresh token for user: {}, device: {}", 
                oldToken.getUser().getId(), oldToken.getDeviceId());
        
        return savedToken;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TokenRefreshException("User not found"));
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public void revokeTokenByDeviceId(Long userId, String deviceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TokenRefreshException("User not found"));
        refreshTokenRepository.findByUserAndDeviceId(user, deviceId)
                .ifPresent(refreshTokenRepository::delete);
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired refresh tokens");
        refreshTokenRepository.deleteAllExpiredTokens(Instant.now());
    }

    @Scheduled(cron = "0 0 0 * * *") // Run daily at midnight
    @Transactional
    public void cleanupInactiveTokens() {
        log.info("Starting cleanup of inactive refresh tokens");
        // Remove tokens not used in 30 days
        Instant threshold = Instant.now().minusSeconds(30 * 24 * 60 * 60);
        List<RefreshToken> inactiveTokens = refreshTokenRepository.findInactiveTokens(threshold);
        refreshTokenRepository.deleteAll(inactiveTokens);
        log.info("Cleaned up {} inactive refresh tokens", inactiveTokens.size());
    }
} 