package com.fix4home.fix4home.model.enums;

public enum NotificationType {
    // Service Request Related
    SERVICE_REQUEST_CREATED("Service Request Created", "🔧", "New service request created"),
    SERVICE_REQUEST_ACCEPTED("Service Request Accepted", "✅", "Your service request has been accepted"),
    SERVICE_REQUEST_COMPLETED("Service Request Completed", "🎉", "Service request has been completed"),
    SERVICE_REQUEST_CANCELLED("Service Request Cancelled", "❌", "Service request has been cancelled"),
    SERVICE_REQUEST_REMINDER("Service Request Reminder", "⏰", "Reminder about upcoming service"),
    
    // Service Post Related
    SERVICE_POST_NEW_RESPONSE("New Service Post Response", "💬", "New response to your service post"),
    SERVICE_POST_SELECTED("Service Post Selected", "🎯", "You were selected for a service post"),
    SERVICE_POST_EXPIRING("Service Post Expiring", "⏳", "Your service post is expiring soon"),
    
    // Payment Related
    PAYMENT_RECEIVED("Payment Received", "💰", "Payment has been received"),
    PAYMENT_PENDING("Payment Pending", "⏳", "Payment is pending"),
    PAYMENT_FAILED("Payment Failed", "❌", "Payment has failed"),
    
    // Chat Related
    NEW_MESSAGE("New Message", "💬", "You have a new message"),
    CHAT_STARTED("Chat Started", "💬", "New chat conversation started"),
    
    // Technician Related
    TECHNICIAN_APPROVED("Technician Approved", "✅", "Your technician profile has been approved"),
    TECHNICIAN_REJECTED("Technician Rejected", "❌", "Your technician profile was rejected"),
    NEW_JOB_OPPORTUNITY("New Job Opportunity", "🔧", "New job opportunity available"),
    RATING_RECEIVED("Rating Received", "⭐", "You received a new rating"),
    
    // System Related
    SYSTEM_MAINTENANCE("System Maintenance", "🔧", "Scheduled system maintenance"),
    SYSTEM_UPDATE("System Update", "🆕", "New system update available"),
    SECURITY_ALERT("Security Alert", "🔒", "Security alert for your account"),
    
    // Marketing
    PROMOTIONAL("Promotional", "🎁", "Special offers and promotions"),
    NEWSLETTER("Newsletter", "📰", "Newsletter and updates"),
    
    // General
    REMINDER("Reminder", "⏰", "General reminder"),
    ANNOUNCEMENT("Announcement", "📢", "Important announcement"),
    CUSTOM("Custom", "ℹ️", "Custom notification");
    
    private final String displayName;
    private final String icon;
    private final String description;
    
    NotificationType(String displayName, String icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isHighPriority() {
        return switch (this) {
            case SERVICE_REQUEST_ACCEPTED, SERVICE_REQUEST_CANCELLED, 
                 PAYMENT_FAILED, SECURITY_ALERT, SYSTEM_MAINTENANCE -> true;
            default -> false;
        };
    }
    
    public boolean isTransactional() {
        return switch (this) {
            case SERVICE_REQUEST_CREATED, SERVICE_REQUEST_ACCEPTED, SERVICE_REQUEST_COMPLETED,
                 SERVICE_REQUEST_CANCELLED, PAYMENT_RECEIVED, PAYMENT_PENDING, PAYMENT_FAILED,
                 TECHNICIAN_APPROVED, TECHNICIAN_REJECTED, SECURITY_ALERT -> true;
            default -> false;
        };
    }
    
    public boolean isMarketing() {
        return this == PROMOTIONAL || this == NEWSLETTER;
    }
    
    public String getDefaultSound() {
        return switch (this) {
            case PAYMENT_RECEIVED -> "cash.wav";
            case NEW_MESSAGE, CHAT_STARTED -> "message.wav";
            case SECURITY_ALERT -> "alert.wav";
            case SERVICE_REQUEST_COMPLETED -> "success.wav";
            default -> "default";
        };
    }
}
