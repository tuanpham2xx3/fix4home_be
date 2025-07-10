package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a complaint is not found in the system
 */
public class ComplaintNotFoundException extends BaseBusinessException {
    
    public ComplaintNotFoundException(Long complaintId) {
        super(
            "COMPLAINT_NOT_FOUND",
            "Complaint not found with ID: " + complaintId,
            "The requested complaint is not found.",
            HttpStatus.NOT_FOUND
        );
    }
    
    public ComplaintNotFoundException(String message) {
        super(
            "COMPLAINT_NOT_FOUND",
            "Complaint not found: " + message,
            "The requested complaint is not found.",
            HttpStatus.NOT_FOUND
        );
    }
    
    // Static factory methods
    public static ComplaintNotFoundException withId(Long complaintId) {
        return new ComplaintNotFoundException(complaintId);
    }
    
    public static ComplaintNotFoundException withMessage(String message) {
        return new ComplaintNotFoundException(message);
    }
} 