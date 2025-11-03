package com.fix4home.fix4home.exception;

/**
 * Exception thrown when a service post response is not found
 */
public class ServicePostResponseNotFoundException extends BaseBusinessException {
    
    public ServicePostResponseNotFoundException(Long id) {
        super("Service post response not found with ID: " + id);
    }
    
    public ServicePostResponseNotFoundException(String message) {
        super(message);
    }
    
    public static ServicePostResponseNotFoundException withId(Long id) {
        return new ServicePostResponseNotFoundException(id);
    }
} 