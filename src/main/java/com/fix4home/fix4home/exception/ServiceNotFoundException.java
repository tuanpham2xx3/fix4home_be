package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a service is not found in the system
 */
public class ServiceNotFoundException extends BaseBusinessException {
    
    public ServiceNotFoundException(Long serviceId) {
        super(
            "SERVICE_NOT_FOUND",
            "Service not found with ID: " + serviceId,
            "The requested service is not available.",
            HttpStatus.NOT_FOUND
        );
    }
    
    public ServiceNotFoundException(String serviceName) {
        super(
            "SERVICE_NOT_FOUND",
            "Service not found with name: " + serviceName,
            "The requested service is not available.",
            HttpStatus.NOT_FOUND
        );
    }
} 