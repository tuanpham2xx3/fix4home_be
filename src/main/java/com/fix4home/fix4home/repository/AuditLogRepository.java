package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for audit log operations
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * Find audit logs by user ID
     */
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Find audit logs by action
     */
    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);
    
    /**
     * Find audit logs by resource
     */
    Page<AuditLog> findByResourceOrderByCreatedAtDesc(String resource, Pageable pageable);
    
    /**
     * Find audit logs by IP address
     */
    Page<AuditLog> findByIpAddressOrderByCreatedAtDesc(String ipAddress, Pageable pageable);
    
    /**
     * Find audit logs within date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate, 
                                   Pageable pageable);
    
    /**
     * Find failed requests (for security monitoring)
     */
    @Query("SELECT a FROM AuditLog a WHERE a.success = false OR a.responseStatus >= 400 ORDER BY a.createdAt DESC")
    Page<AuditLog> findFailedRequests(Pageable pageable);
    
    /**
     * Find suspicious activities (multiple failed attempts from same IP)
     */
    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress AND (a.success = false OR a.responseStatus >= 400) " +
           "AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AuditLog> findSuspiciousActivities(@Param("ipAddress") String ipAddress, 
                                           @Param("since") LocalDateTime since);
    
    /**
     * Count failed attempts by IP in time window
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.ipAddress = :ipAddress " +
           "AND (a.success = false OR a.responseStatus >= 400) " +
           "AND a.createdAt >= :since")
    long countFailedAttemptsByIp(@Param("ipAddress") String ipAddress, 
                                @Param("since") LocalDateTime since);
    
    /**
     * Find audit logs by user and action
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.action = :action " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> findByUserAndAction(@Param("userId") Long userId, 
                                      @Param("action") String action, 
                                      Pageable pageable);
    
    /**
     * Get most accessed endpoints
     */
    @Query("SELECT a.endpoint, COUNT(a) as accessCount FROM AuditLog a " +
           "WHERE a.createdAt >= :since " +
           "GROUP BY a.endpoint " +
           "ORDER BY accessCount DESC")
    List<Object[]> getMostAccessedEndpoints(@Param("since") LocalDateTime since);
    
    /**
     * Get user activity summary
     */
    @Query("SELECT a.action, COUNT(a) as actionCount FROM AuditLog a " +
           "WHERE a.userId = :userId AND a.createdAt >= :since " +
           "GROUP BY a.action " +
           "ORDER BY actionCount DESC")
    List<Object[]> getUserActivitySummary(@Param("userId") Long userId, 
                                         @Param("since") LocalDateTime since);
    
    /**
     * Delete old audit logs (for cleanup)
     */
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
}


