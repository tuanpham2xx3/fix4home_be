package com.fix4home.fix4home.exception;

import java.time.LocalDateTime;

/**
 * Exception thrown when trying to perform operations on an expired service post
 */
public class ServicePostExpiredException extends BaseBusinessException {
    
    public ServicePostExpiredException(Long id, LocalDateTime expiredAt) {
        super("Service post with ID " + id + " expired at " + expiredAt);
    }
    
    public ServicePostExpiredException(String message) {
        super(message);
    }
    
    public static ServicePostExpiredException withId(Long id, LocalDateTime expiredAt) {
        return new ServicePostExpiredException(id, expiredAt);
    }
} 