package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when user provides invalid login credentials
 */
public class InvalidCredentialsException extends BaseBusinessException {
    
    public InvalidCredentialsException() {
        super(
            "INVALID_CREDENTIALS",
            "Invalid username/email or password provided",
            "Invalid username or password. Please try again.",
            HttpStatus.UNAUTHORIZED
        );
    }
    
    public InvalidCredentialsException(String details) {
        super(
            "INVALID_CREDENTIALS", 
            "Invalid credentials: " + details,
            "Invalid username or password. Please try again.",
            HttpStatus.UNAUTHORIZED
        );
    }
} 