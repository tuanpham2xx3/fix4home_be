package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.TechnicianProfile;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianProfileRepository extends JpaRepository<TechnicianProfile, Long> {
    
    Optional<TechnicianProfile> findByUser(User user);
    
    Optional<TechnicianProfile> findByUserId(Long userId);
    
    List<TechnicianProfile> findByStatus(UserStatus status);
    
    Page<TechnicianProfile> findByStatus(UserStatus status, Pageable pageable);
    
    @Query("SELECT tp FROM TechnicianProfile tp WHERE tp.rating >= :minRating ORDER BY tp.rating DESC")
    List<TechnicianProfile> findByRatingGreaterThanEqualOrderByRatingDesc(@Param("minRating") Float minRating);
    
    @Query("SELECT tp FROM TechnicianProfile tp WHERE tp.fullName LIKE %:name%")
    List<TechnicianProfile> findByFullNameContainingIgnoreCase(@Param("name") String name);
    
    // Online/Offline Status queries
    List<TechnicianProfile> findByIsOnlineAndStatus(Boolean isOnline, UserStatus status);
    
    @Query("SELECT tp FROM TechnicianProfile tp WHERE tp.isOnline = true AND tp.status = :status")
    List<TechnicianProfile> findOnlineTechnicians(@Param("status") UserStatus status);
    
    @Query("SELECT tp FROM TechnicianProfile tp WHERE tp.isOnline = true AND tp.status = :status AND tp.currentLatitude IS NOT NULL AND tp.currentLongitude IS NOT NULL")
    List<TechnicianProfile> findOnlineTechniciansWithLocation(@Param("status") UserStatus status);
    
    // Location-based queries
    @Query("SELECT tp FROM TechnicianProfile tp WHERE tp.currentLatitude IS NOT NULL AND tp.currentLongitude IS NOT NULL AND tp.status = :status")
    List<TechnicianProfile> findTechniciansWithLocation(@Param("status") UserStatus status);
    
    @Query("SELECT tp FROM TechnicianProfile tp WHERE tp.status = :status AND tp.isOnline = :isOnline AND tp.currentLatitude IS NOT NULL AND tp.currentLongitude IS NOT NULL")
    List<TechnicianProfile> findByStatusAndIsOnlineWithLocation(@Param("status") UserStatus status, @Param("isOnline") Boolean isOnline);
} 