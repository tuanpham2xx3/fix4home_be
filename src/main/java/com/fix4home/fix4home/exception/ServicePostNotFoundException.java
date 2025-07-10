package com.fix4home.fix4home.exception;

/**
 * Exception thrown when a service post is not found
 */
public class ServicePostNotFoundException extends BaseBusinessException {
    
    public ServicePostNotFoundException(Long id) {
        super("Service post not found with ID: " + id);
    }
    
    public ServicePostNotFoundException(String message) {
        super(message);
    }
    
    public static ServicePostNotFoundException withId(Long id) {
        return new ServicePostNotFoundException(id);
    }
} 