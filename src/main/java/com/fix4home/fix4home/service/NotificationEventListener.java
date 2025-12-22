package com.fix4home.fix4home.service;

import com.fix4home.fix4home.model.dto.notification.CreateNotificationRequest;
import com.fix4home.fix4home.service.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listens to domain events and creates user notifications (and optional push notifications).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    // ==================== SERVICE REQUEST EVENTS ====================

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleServiceRequestCreated(ServiceRequestCreatedEvent event) {
        log.info("Handling ServiceRequestCreatedEvent for serviceRequestId={}", event.getServiceRequestId());

        // Notify customer
        notificationService.createNotification(CreateNotificationRequest.builder()
                .userId(event.getCustomerId())
                .title("Yêu cầu dịch vụ đã được tạo")
                .message("Mã yêu cầu: " + event.getServiceRequestId()
                        + " - Dịch vụ: " + event.getServiceName())
                .build());

        // Notify technician if already assigned
        if (event.getTechnicianId() != null) {
            notificationService.createNotification(CreateNotificationRequest.builder()
                    .userId(event.getTechnicianId())
                    .title("Bạn có yêu cầu dịch vụ mới")
                    .message("Mã yêu cầu: " + event.getServiceRequestId())
                    .build());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleServiceRequestCancelled(ServiceRequestCancelledEvent event) {
        log.info("Handling ServiceRequestCancelledEvent for serviceRequestId={}", event.getServiceRequestId());

        // Customer
        notificationService.createNotification(CreateNotificationRequest.builder()
                .userId(event.getCustomerId())
                .title("Yêu cầu dịch vụ đã được hủy")
                .message("Mã yêu cầu: " + event.getServiceRequestId())
                .build());

        // Technician (if any)
        if (event.getTechnicianId() != null) {
            notificationService.createNotification(CreateNotificationRequest.builder()
                    .userId(event.getTechnicianId())
                    .title("Một công việc đã bị hủy")
                    .message("Mã yêu cầu: " + event.getServiceRequestId())
                    .build());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleServiceRequestStatusChanged(ServiceRequestStatusChangedEvent event) {
        log.info("Handling ServiceRequestStatusChangedEvent for serviceRequestId={} from {} to {}",
                event.getServiceRequestId(), event.getOldStatus(), event.getNewStatus());

        String title;
        String message;

        switch (event.getNewStatus()) {
            case IN_PROGRESS -> {
                title = "Dịch vụ đã được bắt đầu";
                message = "Thợ đã bắt đầu làm việc cho yêu cầu " + event.getServiceRequestId();
            }
            case DONE -> {
                title = "Dịch vụ đã hoàn thành";
                message = "Yêu cầu " + event.getServiceRequestId() + " đã được hoàn thành.";
            }
            default -> {
                // Only notify for important transitions
                return;
            }
        }

        notificationService.createNotification(CreateNotificationRequest.builder()
                .userId(event.getCustomerId())
                .title(title)
                .message(message)
                .build());
    }

    // ==================== SECURITY EVENTS ====================

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordChanged(PasswordChangedEvent event) {
        log.info("Handling PasswordChangedEvent for userId={}", event.getUserId());

        notificationService.createNotification(CreateNotificationRequest.builder()
                .userId(event.getUserId())
                .title("Mật khẩu của bạn vừa được thay đổi")
                .message("Nếu không phải bạn thực hiện, hãy đổi mật khẩu ngay lập tức và liên hệ hỗ trợ.")
                .build());
    }

    // ==================== COMPLAINT EVENTS ====================

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleComplaintCreated(ComplaintCreatedEvent event) {
        log.info("Handling ComplaintCreatedEvent for complaintId={}", event.getComplaintId());

        // Notify complainant
        notificationService.createNotification(CreateNotificationRequest.builder()
                .userId(event.getComplainantId())
                .title("Khiếu nại của bạn đã được tạo")
                .message("Khiếu nại liên quan tới yêu cầu dịch vụ: " + event.getServiceRequestId())
                .build());

        // Notify accused user
        notificationService.createNotification(CreateNotificationRequest.builder()
                .userId(event.getAccusedId())
                .title("Bạn vừa bị khiếu nại")
                .message("Khiếu nại liên quan tới yêu cầu dịch vụ: " + event.getServiceRequestId())
                .build());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleComplaintStatusChanged(ComplaintStatusChangedEvent event) {
        log.info("Handling ComplaintStatusChangedEvent for complaintId={} from {} to {}",
                event.getComplaintId(), event.getOldStatus(), event.getNewStatus());

        // Tùy theo nhu cầu có thể fetch Complaint từ DB để xác định user nhận thông báo.
        // Để tránh truy vấn nặng ở đây, phần gửi chi tiết có thể được bổ sung sau.
    }
}


