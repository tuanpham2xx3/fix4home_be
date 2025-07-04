package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.*;
import com.fix4home.fix4home.model.dto.notification.*;
import com.fix4home.fix4home.model.entity.Notification;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.model.enums.UserStatus;
import com.fix4home.fix4home.repository.NotificationRepository;
import com.fix4home.fix4home.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService extends BaseService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ==================== CREATE OPERATIONS ====================

    @Transactional
    public NotificationDTO createNotification(CreateNotificationRequest request) {
        logBusinessOperation("CREATE_NOTIFICATION", "userId=" + request.getUserId());
        requireRole(Role.ADMIN);

        validateRequired(request, "request");
        validateRequired(request.getUserId(), "userId");
        validateRequired(request.getTitle(), "title");
        validateRequired(request.getMessage(), "message");

        User user = findUserById(request.getUserId());

        Notification notification = Notification.builder()
                .user(user)
                .title(request.getTitle())
                .message(request.getMessage())
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        return convertToDTO(savedNotification);
    }

    @Transactional
    public List<NotificationDTO> createBulkNotifications(CreateNotificationRequest request) {
        logBusinessOperation("CREATE_BULK_NOTIFICATIONS", 
            "userCount=" + (request.getUserIds() != null ? request.getUserIds().size() : "role-based"));
        requireRole(Role.ADMIN);

        validateRequired(request, "request");
        validateRequired(request.getTitle(), "title");
        validateRequired(request.getMessage(), "message");

        List<User> targetUsers;

        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            // Send to specific users
            targetUsers = userRepository.findAllById(request.getUserIds());
            if (targetUsers.size() != request.getUserIds().size()) {
                throw new BusinessValidationException("Some user IDs not found");
            }
        } else if (request.getTargetRole() != null) {
            // Send to all users of specific role
            Role role = Role.valueOf(request.getTargetRole().toUpperCase());
            targetUsers = userRepository.findByRole(role);
        } else {
            throw new BusinessValidationException("Either userIds or targetRole must be specified for bulk notifications");
        }

        List<Notification> notifications = targetUsers.stream()
                .map(user -> Notification.builder()
                        .user(user)
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .isRead(false)
                        .build())
                .collect(Collectors.toList());

        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);

        return savedNotifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== READ OPERATIONS ====================

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUserNotifications(Long userId, int page, int size, String sortBy, String sortDir, Boolean isRead) {
        logBusinessOperation("GET_USER_NOTIFICATIONS", "userId=" + userId, "isRead=" + isRead);

        validatePositiveId(userId, "userId");
        validatePaginationParams(page, size);
        validateSortDirection(sortDir);

        User user = findUserById(userId);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Notification> notificationPage;
        if (isRead != null) {
            notificationPage = notificationRepository.findByUserAndIsRead(user, isRead, pageable);
        } else {
            notificationPage = notificationRepository.findByUser(user, pageable);
        }
        
        return notificationPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(Long notificationId, Long userId) {
        logBusinessOperation("GET_NOTIFICATION", "notificationId=" + notificationId, "userId=" + userId);

        validatePositiveId(notificationId, "notificationId");
        validatePositiveId(userId, "userId");

        Notification notification = findNotificationById(notificationId);

        // Check ownership
        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessValidationException("Access denied: Notification does not belong to the user");
        }

        return convertToDTO(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> searchNotifications(Long userId, String keyword, int page, int size) {
        logBusinessOperation("SEARCH_NOTIFICATIONS", "userId=" + userId, "keyword=" + keyword);

        validatePositiveId(userId, "userId");
        validateRequired(keyword, "keyword");
        validatePaginationParams(page, size);

        User user = findUserById(userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<Notification> notificationPage = notificationRepository.searchByUserAndKeyword(user, keyword, pageable);
        
        return notificationPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getRecentNotifications(Long userId, int days) {
        logBusinessOperation("GET_RECENT_NOTIFICATIONS", "userId=" + userId, "days=" + days);

        validatePositiveId(userId, "userId");
        validatePositive(days, "days");

        User user = findUserById(userId);

        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        List<Notification> notifications = notificationRepository.findRecentByUser(user, sinceDate);

        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== UPDATE OPERATIONS ====================

    @Transactional
    public NotificationDTO markAsRead(Long notificationId, Long userId) {
        logBusinessOperation("MARK_NOTIFICATION_READ", "notificationId=" + notificationId, "userId=" + userId);

        validatePositiveId(notificationId, "notificationId");
        validatePositiveId(userId, "userId");

        Notification notification = findNotificationById(notificationId);

        // Check ownership
        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessValidationException("Access denied: Notification does not belong to the user");
        }

        notification.setIsRead(true);
        Notification updatedNotification = notificationRepository.save(notification);

        return convertToDTO(updatedNotification);
    }

    @Transactional
    public int markNotifications(Long userId, MarkNotificationRequest request) {
        logBusinessOperation("MARK_NOTIFICATIONS", "userId=" + userId);

        validatePositiveId(userId, "userId");
        validateRequired(request, "request");
        validateRequired(request.getIsRead(), "isRead");

        User user = findUserById(userId);

        int updatedCount = 0;

        if (request.getMarkAll() != null && request.getMarkAll()) {
            // Mark all notifications for user
            updatedCount = notificationRepository.updateAllReadStatusByUser(user, request.getIsRead());
        } else if (request.getNotificationIds() != null && !request.getNotificationIds().isEmpty()) {
            // Mark specific notifications (with ownership validation)
            List<Notification> notifications = notificationRepository.findAllById(request.getNotificationIds());
            
            // Validate ownership
            for (Notification notification : notifications) {
                if (!notification.getUser().getId().equals(userId)) {
                    throw new BusinessValidationException("Access denied: One or more notifications do not belong to the user");
                }
            }
            
            updatedCount = notificationRepository.updateMultipleReadStatus(request.getNotificationIds(), request.getIsRead());
        } else if (request.getNotificationId() != null) {
            // Mark single notification
            Notification notification = findNotificationById(request.getNotificationId());
            
            if (!notification.getUser().getId().equals(userId)) {
                throw new BusinessValidationException("Access denied: Notification does not belong to the user");
            }
            
            updatedCount = notificationRepository.updateReadStatus(request.getNotificationId(), request.getIsRead());
        } else {
            throw new BusinessValidationException("Either notificationId, notificationIds, or markAll must be specified");
        }

        return updatedCount;
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        logBusinessOperation("MARK_ALL_NOTIFICATIONS_READ", "userId=" + userId);

        validatePositiveId(userId, "userId");

        User user = findUserById(userId);

        int updatedCount = notificationRepository.updateAllReadStatusByUser(user, true);

        return updatedCount;
    }

    // ==================== DELETE OPERATIONS ====================

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        logBusinessOperation("DELETE_NOTIFICATION", "notificationId=" + notificationId, "userId=" + userId);

        validatePositiveId(notificationId, "notificationId");
        validatePositiveId(userId, "userId");

        Notification notification = findNotificationById(notificationId);

        // Check ownership
        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessValidationException("Access denied: Notification does not belong to the user");
        }

        notificationRepository.delete(notification);
    }

    @Transactional
    public int deleteReadNotifications(Long userId) {
        logBusinessOperation("DELETE_READ_NOTIFICATIONS", "userId=" + userId);

        validatePositiveId(userId, "userId");

        User user = findUserById(userId);

        int deletedCount = notificationRepository.deleteReadNotificationsByUser(user);

        return deletedCount;
    }

    // ==================== STATISTICS ====================

    @Transactional(readOnly = true)
    public NotificationStatsDTO getUserNotificationStats(Long userId) {
        logBusinessOperation("GET_USER_NOTIFICATION_STATS", "userId=" + userId);

        validatePositiveId(userId, "userId");

        User user = findUserById(userId);

        long totalNotifications = notificationRepository.countByUser(user);
        long unreadNotifications = notificationRepository.countByUserAndIsRead(user, false);
        long readNotifications = totalNotifications - unreadNotifications;

        // Calculate percentages
        double readPercentage = totalNotifications > 0 ? (double) readNotifications / totalNotifications * 100 : 0;

        // Recent activity
        LocalDateTime now = LocalDateTime.now();
        long notificationsToday = notificationRepository.countByUserAndDateRange(user, 
                now.toLocalDate().atStartOfDay(), now);
        long notificationsThisWeek = notificationRepository.countByUserAndDateRange(user, 
                now.minusWeeks(1), now);
        long notificationsThisMonth = notificationRepository.countByUserAndDateRange(user, 
                now.minusMonths(1), now);

        // Latest activity
        Notification latestNotification = notificationRepository.findLatestByUser(user);
        LocalDateTime lastNotificationAt = latestNotification != null ? latestNotification.getCreatedAt() : null;

        // For simplicity, mock category and priority data
        Map<String, Long> notificationsByCategory = new HashMap<>();
        notificationsByCategory.put("SERVICE_REQUEST", totalNotifications / 2);
        notificationsByCategory.put("SYSTEM", totalNotifications / 3);
        notificationsByCategory.put("PAYMENT", totalNotifications / 6);

        Map<String, Long> notificationsByPriority = new HashMap<>();
        notificationsByPriority.put("HIGH", totalNotifications / 4);
        notificationsByPriority.put("MEDIUM", totalNotifications / 2);
        notificationsByPriority.put("LOW", totalNotifications / 4);

        return NotificationStatsDTO.builder()
                .userId(userId)
                .totalNotifications(totalNotifications)
                .unreadNotifications(unreadNotifications)
                .readNotifications(readNotifications)
                .readPercentage(readPercentage)
                .notificationsToday(notificationsToday)
                .notificationsThisWeek(notificationsThisWeek)
                .notificationsThisMonth(notificationsThisMonth)
                .lastNotificationAt(lastNotificationAt)
                .lastReadAt(null) // Mock data
                .notificationsByCategory(notificationsByCategory)
                .unreadByCategory(new HashMap<>()) // Mock data
                .notificationsByPriority(notificationsByPriority)
                .build();
    }

    @Transactional(readOnly = true)
    public NotificationStatsDTO getSystemNotificationStats() {
        logBusinessOperation("GET_SYSTEM_NOTIFICATION_STATS");
        requireRole(Role.ADMIN);

        long totalSystemNotifications = notificationRepository.countTotalNotifications();
        long totalUnread = notificationRepository.countUnreadNotifications();
        long totalUsers = userRepository.count();

        double averageNotificationsPerUser = totalUsers > 0 ? (double) totalSystemNotifications / totalUsers : 0;
        double systemReadPercentage = totalSystemNotifications > 0 ? 
                (double) (totalSystemNotifications - totalUnread) / totalSystemNotifications * 100 : 0;

        // System stats
        NotificationStatsDTO.SystemNotificationStats systemStats = NotificationStatsDTO.SystemNotificationStats.builder()
                .totalSystemNotifications(totalSystemNotifications)
                .totalUsers(totalUsers)
                .activeUsers(userRepository.findByStatus(UserStatus.ACTIVE).size())
                .averageNotificationsPerUser(averageNotificationsPerUser)
                .systemReadPercentage(systemReadPercentage)
                .generatedAt(LocalDateTime.now())
                .notificationsByHour(new HashMap<>()) // Mock data
                .notificationsByDay(new HashMap<>()) // Mock data
                .topCategories(new HashMap<>()) // Mock data
                .topPriorities(new HashMap<>()) // Mock data
                .build();

        return NotificationStatsDTO.builder()
                .userId(null) // System-wide stats
                .totalNotifications(totalSystemNotifications)
                .unreadNotifications(totalUnread)
                .readNotifications(totalSystemNotifications - totalUnread)
                .readPercentage(systemReadPercentage)
                .systemStats(systemStats)
                .build();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        logBusinessOperation("GET_UNREAD_COUNT", "userId=" + userId);

        validatePositiveId(userId, "userId");

        User user = findUserById(userId);

        return notificationRepository.countByUserAndIsRead(user, false);
    }

    // ==================== ADMIN OPERATIONS ====================

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getAllNotifications(int page, int size, String sortBy, String sortDir) {
        logBusinessOperation("GET_ALL_NOTIFICATIONS");
        requireRole(Role.ADMIN);

        validatePaginationParams(page, size);
        validateSortDirection(sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Notification> notificationPage = notificationRepository.findAll(pageable);
        
        return notificationPage.map(this::convertToDTO);
    }

    @Transactional
    public int cleanupOldNotifications(int daysOld) {
        logBusinessOperation("CLEANUP_OLD_NOTIFICATIONS", "daysOld=" + daysOld);
        requireRole(Role.ADMIN);

        validatePositive(daysOld, "daysOld");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deletedCount = notificationRepository.deleteOldNotifications(cutoffDate);
        
        return deletedCount;
    }

    // ==================== HELPER METHODS ====================

    private Notification findNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessValidationException("Notification not found with id: " + notificationId));
    }

    private NotificationDTO convertToDTO(Notification notification) {
        String timeAgo = calculateTimeAgo(notification.getCreatedAt());
        
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .timeAgo(timeAgo)
                .userFullName(getUserFullName(notification.getUser()))
                .userEmail(notification.getUser().getEmail())
                .category("GENERAL") // Mock data - would be actual category
                .priority("MEDIUM") // Mock data - would be actual priority
                .build();
    }

    private String calculateTimeAgo(LocalDateTime createdAt) {
        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        
        long days = duration.toDays();
        long hours = duration.toHours();
        long minutes = duration.toMinutes();
        
        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }

    private String getUserFullName(User user) {
        // This would typically fetch from customer/technician profile
        return user.getUsername(); // Simplified for now
    }
} 