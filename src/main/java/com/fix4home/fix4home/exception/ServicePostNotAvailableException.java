package com.fix4home.fix4home.exception;

/**
 * Exception thrown when a service post is not available for responses
 */
public class ServicePostNotAvailableException extends BaseBusinessException {
    
    public ServicePostNotAvailableException(Long id) {
        super("Service post with ID " + id + " is not available for responses");
    }
    
    public ServicePostNotAvailableException(String message) {
        super(message);
    }
    
    public static ServicePostNotAvailableException withId(Long id) {
        return new ServicePostNotAvailableException(id);
    }
    
    public static ServicePostNotAvailableException expired(Long id) {
        return new ServicePostNotAvailableException("Service post with ID " + id + " has expired");
    }
    
    public static ServicePostNotAvailableException maxResponsesReached(Long id) {
        return new ServicePostNotAvailableException("Service post with ID " + id + " has reached maximum responses");
    }
    
    public static ServicePostNotAvailableException wrongStatus(Long id, String status) {
        return new ServicePostNotAvailableException("Service post with ID " + id + " has status " + status + " and cannot receive responses");
    }
} 