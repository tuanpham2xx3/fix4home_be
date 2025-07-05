package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.RefreshToken;
import com.fix4home.fix4home.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserAndDeviceId(User user, String deviceId);
    List<RefreshToken> findAllByUser(User user);
    void deleteByUser(User user);
    
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate <= ?1")
    void deleteAllExpiredTokens(Instant now);
    
    @Query("SELECT r FROM RefreshToken r WHERE r.lastUsedAt < ?1")
    List<RefreshToken> findInactiveTokens(Instant threshold);
} 