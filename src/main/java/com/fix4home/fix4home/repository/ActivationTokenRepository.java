package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.ActivationToken;
import com.fix4home.fix4home.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {

    /**
     * Find activation token by token string
     */
    Optional<ActivationToken> findByToken(String token);

    /**
     * Find active (unused and not expired) token by email and action
     */
    @Query("SELECT at FROM ActivationToken at WHERE at.email = :email AND at.action = :action " +
           "AND at.used = false AND at.expiresAt > :now ORDER BY at.createdAt DESC")
    Optional<ActivationToken> findActiveTokenByEmailAndAction(
        @Param("email") String email, 
        @Param("action") String action, 
        @Param("now") LocalDateTime now
    );

    /**
     * Find all tokens by user and action (for cleanup)
     */
    List<ActivationToken> findByUserAndAction(User user, String action);

    /**
     * Find all tokens by email and action
     */
    List<ActivationToken> findByEmailAndAction(String email, String action);

    /**
     * Delete expired tokens
     */
    @Modifying
    @Query("DELETE FROM ActivationToken at WHERE at.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete used tokens older than specified date
     */
    @Modifying
    @Query("DELETE FROM ActivationToken at WHERE at.used = true AND at.usedAt < :cutoffDate")
    int deleteOldUsedTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Mark all tokens as used for a specific user and action (for security)
     */
    @Modifying
    @Query("UPDATE ActivationToken at SET at.used = true, at.usedAt = :now " +
           "WHERE at.user = :user AND at.action = :action AND at.used = false")
    int markAllTokensAsUsedForUserAndAction(
        @Param("user") User user, 
        @Param("action") String action, 
        @Param("now") LocalDateTime now
    );

    /**
     * Count active tokens by email and action (for rate limiting)
     */
    @Query("SELECT COUNT(at) FROM ActivationToken at WHERE at.email = :email AND at.action = :action " +
           "AND at.used = false AND at.expiresAt > :now")
    long countActiveTokensByEmailAndAction(
        @Param("email") String email, 
        @Param("action") String action, 
        @Param("now") LocalDateTime now
    );

    /**
     * Find tokens sent in the last hour for rate limiting
     */
    @Query("SELECT COUNT(at) FROM ActivationToken at WHERE at.email = :email " +
           "AND at.createdAt > :oneHourAgo")
    long countTokensSentInLastHour(
        @Param("email") String email, 
        @Param("oneHourAgo") LocalDateTime oneHourAgo
    );

    /**
     * Check if user can resend token (60 seconds cooldown)
     */
    @Query("SELECT CASE WHEN COUNT(at) > 0 THEN false ELSE true END FROM ActivationToken at " +
           "WHERE at.email = :email AND at.action = :action AND at.lastSentAt > :cooldownTime")
    boolean canResendToken(
        @Param("email") String email, 
        @Param("action") String action, 
        @Param("cooldownTime") LocalDateTime cooldownTime
    );
}
