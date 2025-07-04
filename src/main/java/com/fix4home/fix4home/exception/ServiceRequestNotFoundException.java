package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a service request is not found
 */
public class ServiceRequestNotFoundException extends BaseBusinessException {
    
    public ServiceRequestNotFoundException(Long requestId) {
        super(
            "SERVICE_REQUEST_NOT_FOUND",
            "Service request not found with ID: " + requestId,
            "The requested service request was not found.",
            HttpStatus.NOT_FOUND
        );
    }
    
    public ServiceRequestNotFoundException(Long requestId, Long userId) {
        super(
            "SERVICE_REQUEST_NOT_FOUND",
            "Service request ID " + requestId + " not found for user ID " + userId,
            "You don't have access to this service request or it doesn't exist.",
            HttpStatus.NOT_FOUND
        );
    }
} 