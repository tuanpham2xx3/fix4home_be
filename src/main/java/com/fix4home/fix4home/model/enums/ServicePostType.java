package com.fix4home.fix4home.model.enums;

/**
 * Service Post Type Enum
 * Defines different types of service posts
 */
public enum ServicePostType {
    URGENT("Cần gấp"),
    CONSULTATION("Tư vấn trước"),
    SCHEDULED("Đặt lịch"),
    QUOTATION("Yêu cầu báo giá");

    private final String description;

    ServicePostType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 