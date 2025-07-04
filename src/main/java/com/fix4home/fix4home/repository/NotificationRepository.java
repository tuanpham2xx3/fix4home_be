package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.Notification;
import com.fix4home.fix4home.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // ==================== BASIC QUERIES ====================
    
    // Find notifications by user
    List<Notification> findByUser(User user);
    
    Page<Notification> findByUser(User user, Pageable pageable);
    
    // Find notifications by user and read status
    List<Notification> findByUserAndIsRead(User user, Boolean isRead);
    
    Page<Notification> findByUserAndIsRead(User user, Boolean isRead, Pageable pageable);
    
    // Find notifications by user ordered by creation time
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // ==================== COUNT QUERIES ====================
    
    // Count total notifications for user
    long countByUser(User user);
    
    // Count unread notifications for user
    long countByUserAndIsRead(User user, Boolean isRead);
    
    // Count notifications by date range
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.createdAt BETWEEN :startDate AND :endDate")
    long countByUserAndDateRange(@Param("user") User user, 
                                @Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
    
    // ==================== SEARCH QUERIES ====================
    
    // Search notifications by title or message
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.message) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Notification> searchByUserAndKeyword(@Param("user") User user, 
                                            @Param("keyword") String keyword, 
                                            Pageable pageable);
    
    // Find recent notifications (last N days)
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.createdAt >= :sinceDate ORDER BY n.createdAt DESC")
    List<Notification> findRecentByUser(@Param("user") User user, @Param("sinceDate") LocalDateTime sinceDate);
    
    // ==================== UPDATE QUERIES ====================
    
    // Mark notification as read/unread
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = :isRead WHERE n.id = :notificationId")
    int updateReadStatus(@Param("notificationId") Long notificationId, @Param("isRead") Boolean isRead);
    
    // Mark all user notifications as read/unread
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = :isRead WHERE n.user = :user")
    int updateAllReadStatusByUser(@Param("user") User user, @Param("isRead") Boolean isRead);
    
    // Mark multiple notifications as read/unread
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = :isRead WHERE n.id IN :notificationIds")
    int updateMultipleReadStatus(@Param("notificationIds") List<Long> notificationIds, @Param("isRead") Boolean isRead);
    
    // Mark unread notifications as read for user (up to specific date)
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false AND n.createdAt <= :beforeDate")
    int markAsReadBeforeDate(@Param("user") User user, @Param("beforeDate") LocalDateTime beforeDate);
    
    // ==================== DELETE QUERIES ====================
    
    // Delete notifications by user
    void deleteByUser(User user);
    
    // Delete read notifications for user
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user = :user AND n.isRead = true")
    int deleteReadNotificationsByUser(@Param("user") User user);
    
    // Delete old notifications (older than specific date)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :beforeDate")
    int deleteOldNotifications(@Param("beforeDate") LocalDateTime beforeDate);
    
    // Delete notifications by user and date range
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user = :user AND n.createdAt BETWEEN :startDate AND :endDate")
    int deleteByUserAndDateRange(@Param("user") User user, 
                                @Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
    
    // ==================== ADMIN/STATS QUERIES ====================
    
    // Count total notifications in system
    @Query("SELECT COUNT(n) FROM Notification n")
    long countTotalNotifications();
    
    // Count unread notifications in system
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isRead = false")
    long countUnreadNotifications();
    
    // Get users with most notifications
    @Query("SELECT n.user, COUNT(n) FROM Notification n GROUP BY n.user ORDER BY COUNT(n) DESC")
    List<Object[]> findUsersWithMostNotifications(Pageable pageable);
    
    // Get notification statistics by date
    @Query("SELECT DATE(n.createdAt) as date, COUNT(n) as count FROM Notification n " +
           "WHERE n.createdAt BETWEEN :startDate AND :endDate GROUP BY DATE(n.createdAt) ORDER BY date")
    List<Object[]> getNotificationStatsByDate(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    // Find notifications created in date range
    List<Notification> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find latest notification for user
    @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.createdAt DESC LIMIT 1")
    Notification findLatestByUser(@Param("user") User user);
} 