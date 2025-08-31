package com.fix4home.fix4home.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix4home.fix4home.config.PushNotificationConfig;
import com.fix4home.fix4home.exception.BusinessValidationException;
import com.fix4home.fix4home.exception.ResourceNotFoundException;
import com.fix4home.fix4home.model.entity.DeviceToken;
import com.fix4home.fix4home.model.entity.PushNotification;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.DevicePlatform;
import com.fix4home.fix4home.model.enums.NotificationStatus;
import com.fix4home.fix4home.model.enums.NotificationType;
import com.fix4home.fix4home.repository.DeviceTokenRepository;
import com.fix4home.fix4home.repository.PushNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PushNotificationService extends BaseService {
    
    private final PushNotificationRepository pushNotificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final PushNotificationConfig config;
    private final ObjectMapper objectMapper;
    
    /**
     * Send push notification to user
     */
    @Async
    public void sendNotificationToUser(User user, NotificationType type, String title, 
                                     String message, Map<String, Object> data) {
        if (!config.isEnabled()) {
            log.debug("Push notifications are disabled");
            return;
        }
        
        List<DeviceToken> activeTokens = deviceTokenRepository.findByUserAndIsActiveTrueAndNotificationsEnabledTrue(user);
        
        if (activeTokens.isEmpty()) {
            log.debug("No active device tokens found for user: {}", user.getId());
            return;
        }
        
        for (DeviceToken token : activeTokens) {
            try {
                PushNotification notification = createNotification(user, token, type, title, message, data);
                notification = pushNotificationRepository.save(notification);
                
                // Send immediately
                sendNotification(notification);
                
            } catch (Exception ex) {
                log.error("Failed to send notification to device token: {}", token.getId(), ex);
            }
        }
    }
    
    /**
     * Send scheduled notification
     */
    public void scheduleNotification(User user, NotificationType type, String title, 
                                   String message, LocalDateTime scheduledAt, Map<String, Object> data) {
        List<DeviceToken> activeTokens = deviceTokenRepository.findByUserAndIsActiveTrueAndNotificationsEnabledTrue(user);
        
        for (DeviceToken token : activeTokens) {
            PushNotification notification = createNotification(user, token, type, title, message, data);
            notification.setScheduledAt(scheduledAt);
            pushNotificationRepository.save(notification);
        }
        
        log.info("Scheduled {} notifications for user: {} at: {}", activeTokens.size(), user.getId(), scheduledAt);
    }
    
    /**
     * Send broadcast notification to all users
     */
    @Async
    public void sendBroadcastNotification(NotificationType type, String title, String message, 
                                        Map<String, Object> data) {
        if (!config.isEnabled()) {
            log.debug("Push notifications are disabled");
            return;
        }
        
        // Send to Android devices via FCM
        List<DeviceToken> androidTokens = deviceTokenRepository.findByPlatformAndIsActiveTrueAndNotificationsEnabledTrue(DevicePlatform.ANDROID);
        sendBatchNotifications(androidTokens, type, title, message, data);
        
        // Send to iOS devices via APNs
        List<DeviceToken> iosTokens = deviceTokenRepository.findByPlatformAndIsActiveTrueAndNotificationsEnabledTrue(DevicePlatform.IOS);
        sendBatchNotifications(iosTokens, type, title, message, data);
        
        // Send to Web devices via FCM
        List<DeviceToken> webTokens = deviceTokenRepository.findByPlatformAndIsActiveTrueAndNotificationsEnabledTrue(DevicePlatform.WEB);
        sendBatchNotifications(webTokens, type, title, message, data);
        
        log.info("Broadcast notification sent to {} total devices", androidTokens.size() + iosTokens.size() + webTokens.size());
    }
    
    /**
     * Register device token
     */
    public DeviceToken registerDeviceToken(User user, String token, DevicePlatform platform, 
                                         String deviceId, String deviceName, String appVersion) {
        // Check if token already exists
        deviceTokenRepository.findByTokenAndIsActiveTrue(token)
                .ifPresent(existingToken -> {
                    if (!existingToken.getUser().getId().equals(user.getId())) {
                        // Token moved to different user, deactivate old one
                        existingToken.setIsActive(false);
                        deviceTokenRepository.save(existingToken);
                    }
                });
        
        // Deactivate old tokens for same device
        List<DeviceToken> existingDeviceTokens = deviceTokenRepository.findByUserAndPlatformAndIsActiveTrue(user, platform);
        existingDeviceTokens.stream()
                .filter(dt -> deviceId != null && deviceId.equals(dt.getDeviceId()))
                .forEach(dt -> {
                    dt.setIsActive(false);
                    deviceTokenRepository.save(dt);
                });
        
        DeviceToken deviceToken = DeviceToken.builder()
                .user(user)
                .token(token)
                .platform(platform)
                .deviceId(deviceId)
                .deviceName(deviceName)
                .appVersion(appVersion)
                .isActive(true)
                .notificationsEnabled(true)
                .build();
        
        deviceToken.markAsUsed();
        deviceToken = deviceTokenRepository.save(deviceToken);
        
        log.info("Registered device token for user: {}, platform: {}", user.getId(), platform);
        
        return deviceToken;
    }
    
    /**
     * Update device token status
     */
    public void updateDeviceTokenStatus(Long tokenId, Boolean notificationsEnabled) {
        DeviceToken token = deviceTokenRepository.findById(tokenId)
                .orElseThrow(() -> ResourceNotFoundException.withMessage("Device token not found"));
        
        User currentUser = getCurrentUser();
        if (!token.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessValidationException("You can only update your own device tokens");
        }
        
        token.setNotificationsEnabled(notificationsEnabled);
        deviceTokenRepository.save(token);
    }
    
    /**
     * Get user's device tokens
     */
    @Transactional(readOnly = true)
    public List<DeviceToken> getUserDeviceTokens(User user) {
        return deviceTokenRepository.findByUserAndIsActiveTrueOrderByLastUsedAtDesc(user);
    }
    
    /**
     * Process pending notifications
     */
    @Async
    public void processPendingNotifications() {
        if (!config.isEnabled()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = now.minusHours(24); // Don't send notifications older than 24 hours
        
        List<PushNotification> pendingNotifications = pushNotificationRepository
                .findPendingNotificationsToSend(NotificationStatus.PENDING, now, cutoffTime);
        
        log.info("Processing {} pending notifications", pendingNotifications.size());
        
        for (PushNotification notification : pendingNotifications) {
            try {
                sendNotification(notification);
            } catch (Exception ex) {
                log.error("Failed to send pending notification: {}", notification.getId(), ex);
                notification.markAsFailed("Processing error: " + ex.getMessage());
                pushNotificationRepository.save(notification);
            }
        }
        
        // Process retryable failed notifications
        processRetryableNotifications();
        
        // Mark expired notifications
        markExpiredNotifications();
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    private PushNotification createNotification(User user, DeviceToken token, NotificationType type,
                                              String title, String message, Map<String, Object> data) {
        try {
            String dataPayload = data != null ? objectMapper.writeValueAsString(data) : null;
            
            return PushNotification.builder()
                    .user(user)
                    .deviceToken(token)
                    .type(type)
                    .title(title)
                    .message(message)
                    .dataPayload(dataPayload)
                    .sound(type.getDefaultSound())
                    .priority(type.isHighPriority() ? "high" : "normal")
                    .build();
                    
        } catch (Exception ex) {
            throw new BusinessValidationException("Failed to create notification: " + ex.getMessage());
        }
    }
    
    private void sendNotification(PushNotification notification) {
        try {
            DeviceToken token = notification.getDeviceToken();
            
            if (token.isFCM()) {
                sendFCMNotification(notification);
            } else if (token.isAPNS()) {
                sendAPNSNotification(notification);
            }
            
            token.markAsUsed();
            deviceTokenRepository.save(token);
            
        } catch (Exception ex) {
            log.error("Failed to send notification: {}", notification.getId(), ex);
            notification.markAsFailed("Send error: " + ex.getMessage());
            
            DeviceToken token = notification.getDeviceToken();
            token.markAsFailed();
            deviceTokenRepository.save(token);
            
            pushNotificationRepository.save(notification);
        }
    }
    
    private void sendFCMNotification(PushNotification notification) {
        // FCM implementation would go here
        // For now, simulate success
        notification.markAsSent("fcm_" + System.currentTimeMillis());
        pushNotificationRepository.save(notification);
        
        log.debug("FCM notification sent: {}", notification.getId());
    }
    
    private void sendAPNSNotification(PushNotification notification) {
        // APNS implementation would go here
        // For now, simulate success
        notification.markAsSent("apns_" + System.currentTimeMillis());
        pushNotificationRepository.save(notification);
        
        log.debug("APNS notification sent: {}", notification.getId());
    }
    
    private void sendBatchNotifications(List<DeviceToken> tokens, NotificationType type, 
                                      String title, String message, Map<String, Object> data) {
        for (DeviceToken token : tokens) {
            try {
                PushNotification notification = createNotification(token.getUser(), token, type, title, message, data);
                notification = pushNotificationRepository.save(notification);
                sendNotification(notification);
            } catch (Exception ex) {
                log.error("Failed to send batch notification to token: {}", token.getId(), ex);
            }
        }
    }
    
    private void processRetryableNotifications() {
        List<PushNotification> retryableNotifications = pushNotificationRepository
                .findRetryableNotifications(NotificationStatus.FAILED);
        
        for (PushNotification notification : retryableNotifications) {
            try {
                sendNotification(notification);
            } catch (Exception ex) {
                log.error("Failed to retry notification: {}", notification.getId(), ex);
            }
        }
    }
    
    private void markExpiredNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<PushNotification> expiredNotifications = pushNotificationRepository
                .findExpiredNotifications(NotificationStatus.PENDING, now);
        
        for (PushNotification notification : expiredNotifications) {
            notification.setStatus(NotificationStatus.EXPIRED);
            pushNotificationRepository.save(notification);
        }
        
        if (!expiredNotifications.isEmpty()) {
            log.info("Marked {} notifications as expired", expiredNotifications.size());
        }
    }
}
