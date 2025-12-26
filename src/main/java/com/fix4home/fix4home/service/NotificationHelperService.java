package com.fix4home.fix4home.service;

import com.fix4home.fix4home.model.entity.Booking;
import com.fix4home.fix4home.model.entity.Notification;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.NotificationTypeEnum;
import com.fix4home.fix4home.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationHelperService {

    private final NotificationRepository notificationRepository;

    /**
     * Create notification when booking is created
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createBookingNotification(User user, Booking booking) {
        try {
            Notification notification = Notification.builder()
                    .user(user)
                    .title("Đặt lịch thành công")
                    .message("Bạn đã đặt lịch dịch vụ: " + booking.getTitle() + ". Chúng tôi sẽ liên hệ với bạn sớm nhất.")
                    .type(NotificationTypeEnum.SUCCESS)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            log.debug("Created booking notification for user: {}, booking: {}", user.getId(), booking.getId());
        } catch (Exception e) {
            log.error("Failed to create booking notification for user: {}, booking: {}", user.getId(), booking.getId(), e);
            // Don't throw exception - notification failure shouldn't affect booking creation
        }
    }

    /**
     * Create notification when booking is cancelled
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createCancelBookingNotification(User user, Booking booking) {
        try {
            Notification notification = Notification.builder()
                    .user(user)
                    .title("Đã hủy đặt lịch")
                    .message("Bạn đã hủy đặt lịch dịch vụ: " + booking.getTitle() + ".")
                    .type(NotificationTypeEnum.WARNING)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            log.debug("Created cancel booking notification for user: {}, booking: {}", user.getId(), booking.getId());
        } catch (Exception e) {
            log.error("Failed to create cancel booking notification for user: {}, booking: {}", user.getId(), booking.getId(), e);
            // Don't throw exception - notification failure shouldn't affect booking cancellation
        }
    }

    /**
     * Create notification when user logs in
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createLoginNotification(User user) {
        try {
            Notification notification = Notification.builder()
                    .user(user)
                    .title("Đăng nhập thành công")
                    .message("Bạn đã đăng nhập vào tài khoản thành công.")
                    .type(NotificationTypeEnum.INFO)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            log.debug("Created login notification for user: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to create login notification for user: {}", user.getId(), e);
            // Don't throw exception - notification failure shouldn't affect login
        }
    }

    /**
     * Create welcome notification when user registers
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createRegisterNotification(User user) {
        try {
            Notification notification = Notification.builder()
                    .user(user)
                    .title("Chào mừng bạn!")
                    .message("Cảm ơn bạn đã đăng ký tài khoản. Chúc bạn có trải nghiệm tuyệt vời với Fix4Home.")
                    .type(NotificationTypeEnum.INFO)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            log.debug("Created register notification for user: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to create register notification for user: {}", user.getId(), e);
            // Don't throw exception - notification failure shouldn't affect registration
        }
    }
}

