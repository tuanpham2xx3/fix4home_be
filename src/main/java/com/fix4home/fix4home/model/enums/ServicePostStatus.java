package com.fix4home.fix4home.model.enums;

/**
 * Service Post Status Enum
 * Defines different statuses of service posts
 */
public enum ServicePostStatus {
    DRAFT("Bản nháp"),
    POSTED("Đã đăng"),
    RESPONSES_RECEIVED("Có phản hồi"),
    TECHNICIAN_SELECTED("Đã chọn thợ"),
    IN_PROGRESS("Đang thực hiện"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đã hủy"),
    EXPIRED("Hết hạn");

    private final String description;

    ServicePostStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 