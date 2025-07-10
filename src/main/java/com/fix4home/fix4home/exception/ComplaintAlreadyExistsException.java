package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when trying to create a complaint that already exists
 */
public class ComplaintAlreadyExistsException extends BaseBusinessException {
    
    public ComplaintAlreadyExistsException(Long serviceRequestId, Long complainantId) {
        super(
            "COMPLAINT_ALREADY_EXISTS",
            "Complaint already exists for service request ID: " + serviceRequestId + " by user ID: " + complainantId,
            "You have already filed a complaint for this service request.",
            HttpStatus.CONFLICT
        );
    }
    
    public ComplaintAlreadyExistsException(String message) {
        super(
            "COMPLAINT_ALREADY_EXISTS",
            "Complaint already exists: " + message,
            "A complaint already exists for this request.",
            HttpStatus.CONFLICT
        );
    }
    
    // Static factory methods
    public static ComplaintAlreadyExistsException forServiceRequest(Long serviceRequestId, Long complainantId) {
        return new ComplaintAlreadyExistsException(serviceRequestId, complainantId);
    }
    
    public static ComplaintAlreadyExistsException withMessage(String message) {
        return new ComplaintAlreadyExistsException(message);
    }
} 