package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a service is not available
 */
public class ServiceNotAvailableException extends BaseBusinessException {
    
    public ServiceNotAvailableException(String message) {
        super(
            "SERVICE_NOT_AVAILABLE",
            message,
            "The requested service is currently not available.",
            HttpStatus.BAD_REQUEST
        );
    }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Create exception for service not available by ID
     */
    public static ServiceNotAvailableException withId(Long serviceId) {
        return new ServiceNotAvailableException(
            "Service not available with ID: " + serviceId
        );
    }

    /**
     * Create exception for service not available by name
     */
    public static ServiceNotAvailableException withName(String serviceName) {
        return new ServiceNotAvailableException(
            "Service not available: " + serviceName
        );
    }

    /**
     * Create exception for temporarily unavailable service
     */
    public static ServiceNotAvailableException temporarilyUnavailable(Long serviceId) {
        return new ServiceNotAvailableException(
            "Service temporarily unavailable with ID: " + serviceId
        );
    }
} 