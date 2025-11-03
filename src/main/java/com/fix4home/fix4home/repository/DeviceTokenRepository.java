package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.DeviceToken;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.DevicePlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    
    // Find device tokens by user
    List<DeviceToken> findByUserAndIsActiveTrueOrderByLastUsedAtDesc(User user);
    
    // Find device token by token string
    Optional<DeviceToken> findByTokenAndIsActiveTrue(String token);
    
    // Find device tokens by platform
    List<DeviceToken> findByPlatformAndIsActiveTrueAndNotificationsEnabledTrue(DevicePlatform platform);
    
    // Find device tokens by user and platform
    List<DeviceToken> findByUserAndPlatformAndIsActiveTrue(User user, DevicePlatform platform);
    
    // Find active tokens for a user
    List<DeviceToken> findByUserAndIsActiveTrueAndNotificationsEnabledTrue(User user);
    
    // Find stale tokens that haven't been used recently
    @Query("SELECT dt FROM DeviceToken dt WHERE dt.isActive = true AND " +
           "(dt.lastUsedAt IS NULL AND dt.createdAt < :cutoffDate) OR " +
           "(dt.lastUsedAt < :cutoffDate)")
    List<DeviceToken> findStaleTokens(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Find tokens with many failed attempts
    @Query("SELECT dt FROM DeviceToken dt WHERE dt.isActive = true AND dt.failedAttempts >= :maxFailures")
    List<DeviceToken> findTokensWithManyFailures(@Param("maxFailures") int maxFailures);
    
    // Count active tokens for user
    Long countByUserAndIsActiveTrue(User user);
    
    // Count tokens by platform
    Long countByPlatformAndIsActiveTrue(DevicePlatform platform);
    
    // Deactivate stale tokens
    @Modifying
    @Query("UPDATE DeviceToken dt SET dt.isActive = false WHERE " +
           "(dt.lastUsedAt IS NULL AND dt.createdAt < :cutoffDate) OR " +
           "(dt.lastUsedAt < :cutoffDate)")
    int deactivateStaleTokens(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Deactivate tokens with too many failures
    @Modifying
    @Query("UPDATE DeviceToken dt SET dt.isActive = false WHERE dt.failedAttempts >= :maxFailures")
    int deactivateFailedTokens(@Param("maxFailures") int maxFailures);
    
    // Find duplicate tokens
    @Query("SELECT dt FROM DeviceToken dt WHERE dt.token = :token AND dt.user != :user AND dt.isActive = true")
    List<DeviceToken> findDuplicateTokens(@Param("token") String token, @Param("user") User user);
}
