package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.dto.notification.*;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.repository.UserRepository;
import com.fix4home.fix4home.security.SecurityConstants;
import com.fix4home.fix4home.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Management", description = "APIs for managing notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // Helper method to get current user ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    // ==================== USER NOTIFICATION OPERATIONS ====================

    @GetMapping("/my")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get my notifications")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Boolean isRead) {

        Long userId = getCurrentUserId();
        Page<NotificationDTO> notifications = notificationService.getUserNotifications(userId, page, size, sortBy, sortDir, isRead);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
    }

    @GetMapping("/{notificationId}")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<ApiResponse<NotificationDTO>> getNotificationById(@PathVariable Long notificationId) {
        Long userId = getCurrentUserId();
        NotificationDTO notification = notificationService.getNotificationById(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification retrieved successfully", notification));
    }

    @GetMapping("/unread-count")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        Long userId = getCurrentUserId();
        long unreadCount = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", unreadCount));
    }

    @GetMapping("/search")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Search notifications")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> searchNotifications(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = getCurrentUserId();
        Page<NotificationDTO> notifications = notificationService.searchNotifications(userId, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", notifications));
    }

    @GetMapping("/recent")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get recent notifications")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getRecentNotifications(
            @RequestParam(defaultValue = "7") int days) {

        Long userId = getCurrentUserId();
        List<NotificationDTO> notifications = notificationService.getRecentNotifications(userId, days);
        return ResponseEntity.ok(ApiResponse.success("Recent notifications retrieved successfully", notifications));
    }

    // ==================== NOTIFICATION ACTIONS ====================

    @PutMapping("/{notificationId}/read")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<NotificationDTO>> markAsRead(@PathVariable Long notificationId) {
        Long userId = getCurrentUserId();
        NotificationDTO notification = notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read successfully", notification));
    }

    @PutMapping("/mark")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Mark notifications")
    public ResponseEntity<ApiResponse<Integer>> markNotifications(@Valid @RequestBody MarkNotificationRequest markRequest) {
        Long userId = getCurrentUserId();
        int updatedCount = notificationService.markNotifications(userId, markRequest);
        String message = updatedCount + " notification(s) marked " + (markRequest.getIsRead() ? "read" : "unread") + " successfully";
        return ResponseEntity.ok(ApiResponse.success(message, updatedCount));
    }

    @PutMapping("/mark-all-read")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Mark all as read")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead() {
        Long userId = getCurrentUserId();
        int updatedCount = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(updatedCount + " notification(s) marked as read successfully", updatedCount));
    }

    @DeleteMapping("/{notificationId}")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Delete notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long notificationId) {
        Long userId = getCurrentUserId();
        notificationService.deleteNotification(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }

    @DeleteMapping("/read")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Delete read notifications")
    public ResponseEntity<ApiResponse<Integer>> deleteReadNotifications() {
        Long userId = getCurrentUserId();
        int deletedCount = notificationService.deleteReadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(deletedCount + " read notification(s) deleted successfully", deletedCount));
    }

    // ==================== STATISTICS ====================

    @GetMapping("/stats")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get notification statistics")
    public ResponseEntity<ApiResponse<NotificationStatsDTO>> getNotificationStats() {
        Long userId = getCurrentUserId();
        NotificationStatsDTO stats = notificationService.getUserNotificationStats(userId);
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
    }

    // ==================== ADMIN OPERATIONS ====================

    @PostMapping("/create")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Create notification")
    public ResponseEntity<ApiResponse<NotificationDTO>> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        NotificationDTO notification = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification created successfully", notification));
    }

    @PostMapping("/bulk")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Create bulk notifications")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> createBulkNotifications(@Valid @RequestBody CreateNotificationRequest request) {
        List<NotificationDTO> notifications = notificationService.createBulkNotifications(request);
        String message = notifications.size() + " bulk notification(s) created successfully";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, notifications));
    }

    @GetMapping("/admin/all")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get all notifications")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<NotificationDTO> notifications = notificationService.getAllNotifications(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("All notifications retrieved successfully", notifications));
    }

    @GetMapping("/admin/stats")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get system notification statistics")
    public ResponseEntity<ApiResponse<NotificationStatsDTO>> getSystemNotificationStats() {
        NotificationStatsDTO stats = notificationService.getSystemNotificationStats();
        return ResponseEntity.ok(ApiResponse.success("System statistics retrieved successfully", stats));
    }

    @DeleteMapping("/admin/cleanup/{daysOld}")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Cleanup old notifications")
    public ResponseEntity<ApiResponse<Integer>> cleanupOldNotifications(@PathVariable int daysOld) {
        int deletedCount = notificationService.cleanupOldNotifications(daysOld);
        return ResponseEntity.ok(ApiResponse.success(deletedCount + " old notification(s) cleaned up successfully", deletedCount));
    }
} 