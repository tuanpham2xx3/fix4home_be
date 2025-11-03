package com.fix4home.fix4home.exception;

import com.fix4home.fix4home.model.enums.ServiceRequestStatus;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a service request status operation is invalid
 */
public class InvalidServiceRequestStatusException extends BaseBusinessException {
    
    public InvalidServiceRequestStatusException(String message) {
        super(
            "INVALID_SERVICE_REQUEST_STATUS",
            message,
            "The service request status operation is not allowed.",
            HttpStatus.BAD_REQUEST
        );
    }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Create exception for invalid status transition
     */
    public static InvalidServiceRequestStatusException invalidTransition(
            ServiceRequestStatus from, ServiceRequestStatus to) {
        return new InvalidServiceRequestStatusException(
            "Invalid status transition from " + from + " to " + to
        );
    }

    /**
     * Create exception for cannot cancel in current status
     */
    public static InvalidServiceRequestStatusException cannotCancel(ServiceRequestStatus currentStatus) {
        return new InvalidServiceRequestStatusException(
            "Cannot cancel service request in " + currentStatus + " status"
        );
    }

    /**
     * Create exception for cannot assign in current status
     */
    public static InvalidServiceRequestStatusException cannotAssign(ServiceRequestStatus currentStatus) {
        return new InvalidServiceRequestStatusException(
            "Cannot assign technician to service request in " + currentStatus + " status"
        );
    }

    /**
     * Create exception for cannot start work in current status
     */
    public static InvalidServiceRequestStatusException cannotStartWork(ServiceRequestStatus currentStatus) {
        return new InvalidServiceRequestStatusException(
            "Cannot start work on service request in " + currentStatus + " status"
        );
    }

    /**
     * Create exception for cannot complete work in current status
     */
    public static InvalidServiceRequestStatusException cannotComplete(ServiceRequestStatus currentStatus) {
        return new InvalidServiceRequestStatusException(
            "Cannot complete service request in " + currentStatus + " status"
        );
    }
} 