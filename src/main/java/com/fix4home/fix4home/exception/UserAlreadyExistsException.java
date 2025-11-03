package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to create a user that already exists
 */
public class UserAlreadyExistsException extends BaseBusinessException {
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String field, String value) {
        super(String.format("User with %s '%s' already exists", field, value));
    }
} 