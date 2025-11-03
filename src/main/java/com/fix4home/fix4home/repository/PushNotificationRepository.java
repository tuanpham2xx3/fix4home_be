package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.DeviceToken;
import com.fix4home.fix4home.model.entity.PushNotification;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.NotificationStatus;
import com.fix4home.fix4home.model.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {
    
    // Find notifications by user
    Page<PushNotification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find notifications by status
    List<PushNotification> findByStatusOrderByCreatedAtAsc(NotificationStatus status);
    
    // Find pending notifications ready to send
    @Query("SELECT pn FROM PushNotification pn WHERE pn.status = :status AND " +
           "(pn.scheduledAt IS NULL OR pn.scheduledAt <= :now) AND " +
           "pn.createdAt >= :cutoffTime ORDER BY pn.createdAt ASC")
    List<PushNotification> findPendingNotificationsToSend(@Param("status") NotificationStatus status,
                                                          @Param("now") LocalDateTime now,
                                                          @Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Find failed notifications that can be retried
    @Query("SELECT pn FROM PushNotification pn WHERE pn.status = :status AND " +
           "pn.retryCount < pn.maxRetries ORDER BY pn.failedAt ASC")
    List<PushNotification> findRetryableNotifications(@Param("status") NotificationStatus status);
    
    // Find notifications by device token
    List<PushNotification> findByDeviceTokenOrderByCreatedAtDesc(DeviceToken deviceToken);
    
    // Find notifications by type
    List<PushNotification> findByTypeAndUserOrderByCreatedAtDesc(NotificationType type, User user);
    
    // Count unread notifications for user
    @Query("SELECT COUNT(pn) FROM PushNotification pn WHERE pn.user = :user AND " +
           "pn.status = :deliveredStatus AND pn.clickedAt IS NULL")
    Long countUnreadNotifications(@Param("user") User user, @Param("deliveredStatus") NotificationStatus deliveredStatus);
    
    // Find recent notifications for user
    @Query("SELECT pn FROM PushNotification pn WHERE pn.user = :user AND " +
           "pn.createdAt >= :since ORDER BY pn.createdAt DESC")
    List<PushNotification> findRecentNotifications(@Param("user") User user, @Param("since") LocalDateTime since);
    
    // Get notification statistics
    @Query("SELECT pn.status, COUNT(pn) FROM PushNotification pn WHERE pn.createdAt >= :since " +
           "GROUP BY pn.status")
    List<Object[]> getNotificationStatistics(@Param("since") LocalDateTime since);
    
    // Get notification statistics by type
    @Query("SELECT pn.type, COUNT(pn), AVG(CASE WHEN pn.sentAt IS NOT NULL THEN " +
           "EXTRACT(EPOCH FROM (pn.sentAt - pn.createdAt)) ELSE NULL END) " +
           "FROM PushNotification pn WHERE pn.createdAt >= :since " +
           "GROUP BY pn.type")
    List<Object[]> getNotificationStatisticsByType(@Param("since") LocalDateTime since);
    
    // Find expired notifications
    @Query("SELECT pn FROM PushNotification pn WHERE pn.status = :pendingStatus AND " +
           "pn.timeToLive IS NOT NULL AND " +
           "pn.createdAt <= :expirationTime")
    List<PushNotification> findExpiredNotifications(@Param("pendingStatus") NotificationStatus pendingStatus,
                                                   @Param("expirationTime") LocalDateTime expirationTime);
    
    // Count notifications by user and type in time period
    @Query("SELECT COUNT(pn) FROM PushNotification pn WHERE pn.user = :user AND " +
           "pn.type = :type AND pn.createdAt >= :since")
    Long countNotificationsByUserAndType(@Param("user") User user, 
                                        @Param("type") NotificationType type, 
                                        @Param("since") LocalDateTime since);
    
    // Find notifications by external ID
    List<PushNotification> findByExternalId(String externalId);
}
