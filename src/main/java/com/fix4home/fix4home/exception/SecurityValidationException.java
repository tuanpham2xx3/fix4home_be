package com.fix4home.fix4home.exception;

/**
 * Exception thrown when security validation fails
 */
public class SecurityValidationException extends RuntimeException {
    
    public SecurityValidationException(String message) {
        super(message);
    }
    
    public SecurityValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}


