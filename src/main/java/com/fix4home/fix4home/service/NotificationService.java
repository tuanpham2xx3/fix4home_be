package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.BadRequestException;
import com.fix4home.fix4home.model.dto.notification.*;
import com.fix4home.fix4home.model.entity.Notification;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.Role;
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
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ==================== CREATE OPERATIONS ====================

    @Transactional
    public NotificationDTO createNotification(CreateNotificationRequest request) {
        log.info("Creating notification for user ID: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BadRequestException("User not found with id: " + request.getUserId()));

        Notification notification = Notification.builder()
                .user(user)
                .title(request.getTitle())
                .message(request.getMessage())
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created successfully with ID: {}", savedNotification.getId());

        return convertToDTO(savedNotification);
    }

    @Transactional
    public List<NotificationDTO> createBulkNotifications(CreateNotificationRequest request) {
        log.info("Creating bulk notifications for {} users", 
                 request.getUserIds() != null ? request.getUserIds().size() : "role-based");

        List<User> targetUsers;

        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            // Send to specific users
            targetUsers = userRepository.findAllById(request.getUserIds());
            if (targetUsers.size() != request.getUserIds().size()) {
                throw new BadRequestException("Some user IDs not found");
            }
        } else if (request.getTargetRole() != null) {
            // Send to all users of specific role
            Role role = Role.valueOf(request.getTargetRole().toUpperCase());
            targetUsers = userRepository.findByRole(role);
        } else {
            throw new BadRequestException("Either userIds or targetRole must be specified for bulk notifications");
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
        log.info("Bulk notifications created successfully: {} notifications", savedNotifications.size());

        return savedNotifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== READ OPERATIONS ====================

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUserNotifications(Long userId, int page, int size, String sortBy, String sortDir, Boolean isRead) {
        log.info("Fetching notifications for user ID: {} - page: {}, size: {}, read: {}", userId, page, size, isRead);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

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
        log.info("Fetching notification ID: {} for user ID: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BadRequestException("Notification not found with id: " + notificationId));

        // Check ownership
        if (!notification.getUser().getId().equals(userId)) {
            throw new BadRequestException("Access denied: Notification does not belong to the user");
        }

        return convertToDTO(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> searchNotifications(Long userId, String keyword, int page, int size) {
        log.info("Searching notifications for user ID: {} with keyword: {}", userId, keyword);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<Notification> notificationPage = notificationRepository.searchByUserAndKeyword(user, keyword, pageable);
        
        return notificationPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getRecentNotifications(Long userId, int days) {
        log.info("Fetching recent notifications for user ID: {} - last {} days", userId, days);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        List<Notification> notifications = notificationRepository.findRecentByUser(user, sinceDate);

        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== UPDATE OPERATIONS ====================

    @Transactional
    public NotificationDTO markAsRead(Long notificationId, Long userId) {
        log.info("Marking notification ID: {} as read for user ID: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BadRequestException("Notification not found with id: " + notificationId));

        // Check ownership
        if (!notification.getUser().getId().equals(userId)) {
            throw new BadRequestException("Access denied: Notification does not belong to the user");
        }

        notification.setIsRead(true);
        Notification updatedNotification = notificationRepository.save(notification);

        log.info("Notification ID: {} marked as read successfully", notificationId);
        return convertToDTO(updatedNotification);
    }

    @Transactional
    public int markNotifications(Long userId, MarkNotificationRequest request) {
        log.info("Processing mark notification request for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        int updatedCount = 0;

        if (request.getMarkAll() != null && request.getMarkAll()) {
            // Mark all notifications for user
            updatedCount = notificationRepository.updateAllReadStatusByUser(user, request.getIsRead());
            log.info("Marked all notifications for user ID: {} as {}: {} notifications updated", 
                     userId, request.getIsRead() ? "read" : "unread", updatedCount);
        } else if (request.getNotificationIds() != null && !request.getNotificationIds().isEmpty()) {
            // Mark specific notifications (with ownership validation)
            List<Notification> notifications = notificationRepository.findAllById(request.getNotificationIds());
            
            // Validate ownership
            for (Notification notification : notifications) {
                if (!notification.getUser().getId().equals(userId)) {
                    throw new BadRequestException("Access denied: One or more notifications do not belong to the user");
                }
            }
            
            updatedCount = notificationRepository.updateMultipleReadStatus(request.getNotificationIds(), request.getIsRead());
            log.info("Marked {} notifications as {} for user ID: {}", 
                     updatedCount, request.getIsRead() ? "read" : "unread", userId);
        } else if (request.getNotificationId() != null) {
            // Mark single notification
            Notification notification = notificationRepository.findById(request.getNotificationId())
                    .orElseThrow(() -> new BadRequestException("Notification not found with id: " + request.getNotificationId()));
            
            if (!notification.getUser().getId().equals(userId)) {
                throw new BadRequestException("Access denied: Notification does not belong to the user");
            }
            
            updatedCount = notificationRepository.updateReadStatus(request.getNotificationId(), request.getIsRead());
            log.info("Marked notification ID: {} as {} for user ID: {}", 
                     request.getNotificationId(), request.getIsRead() ? "read" : "unread", userId);
        } else {
            throw new BadRequestException("Either notificationId, notificationIds, or markAll must be specified");
        }

        return updatedCount;
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        int updatedCount = notificationRepository.updateAllReadStatusByUser(user, true);
        log.info("Marked all notifications as read for user ID: {}: {} notifications updated", userId, updatedCount);

        return updatedCount;
    }

    // ==================== DELETE OPERATIONS ====================

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        log.info("Deleting notification ID: {} for user ID: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BadRequestException("Notification not found with id: " + notificationId));

        // Check ownership
        if (!notification.getUser().getId().equals(userId)) {
            throw new BadRequestException("Access denied: Notification does not belong to the user");
        }

        notificationRepository.delete(notification);
        log.info("Notification ID: {} deleted successfully", notificationId);
    }

    @Transactional
    public int deleteReadNotifications(Long userId) {
        log.info("Deleting read notifications for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        int deletedCount = notificationRepository.deleteReadNotificationsByUser(user);
        log.info("Deleted {} read notifications for user ID: {}", deletedCount, userId);

        return deletedCount;
    }

    // ==================== STATISTICS ====================

    @Transactional(readOnly = true)
    public NotificationStatsDTO getUserNotificationStats(Long userId) {
        log.info("Generating notification statistics for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

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
        log.info("Generating system-wide notification statistics");

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
                .activeUsers(userRepository.findByStatus(com.fix4home.fix4home.model.enums.UserStatus.ACTIVE).size())
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
        log.info("Getting unread notification count for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        return notificationRepository.countByUserAndIsRead(user, false);
    }

    // ==================== ADMIN OPERATIONS ====================

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getAllNotifications(int page, int size, String sortBy, String sortDir) {
        log.info("Admin fetching all notifications - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Notification> notificationPage = notificationRepository.findAll(pageable);
        
        return notificationPage.map(this::convertToDTO);
    }

    @Transactional
    public int cleanupOldNotifications(int daysOld) {
        log.info("Admin cleaning up notifications older than {} days", daysOld);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deletedCount = notificationRepository.deleteOldNotifications(cutoffDate);
        
        log.info("Cleaned up {} old notifications", deletedCount);
        return deletedCount;
    }

    // ==================== HELPER METHODS ====================

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