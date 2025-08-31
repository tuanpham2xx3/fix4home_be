package com.fix4home.fix4home.model.enums;

public enum NotificationStatus {
    PENDING("Pending", "Notification is queued for sending"),
    SENT("Sent", "Notification has been sent to push service"),
    DELIVERED("Delivered", "Notification delivered to device"),
    FAILED("Failed", "Notification failed to send"),
    CANCELLED("Cancelled", "Notification was cancelled"),
    EXPIRED("Expired", "Notification expired before sending");
    
    private final String displayName;
    private final String description;
    
    NotificationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isTerminal() {
        return this == DELIVERED || this == FAILED || this == CANCELLED || this == EXPIRED;
    }
    
    public boolean isSuccess() {
        return this == DELIVERED;
    }
    
    public boolean canRetry() {
        return this == FAILED;
    }
}
