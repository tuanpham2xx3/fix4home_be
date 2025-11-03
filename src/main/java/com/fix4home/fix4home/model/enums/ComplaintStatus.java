package com.fix4home.fix4home.model.enums;

/**
 * Enum representing the status of a complaint
 */
public enum ComplaintStatus {
    /**
     * Complaint is waiting for admin review
     */
    PENDING,
    
    /**
     * Complaint is under investigation by admin
     */
    INVESTIGATING,
    
    /**
     * Complaint has been resolved by admin
     */
    RESOLVED,
    
    /**
     * Complaint has been rejected by admin
     */
    REJECTED
} 