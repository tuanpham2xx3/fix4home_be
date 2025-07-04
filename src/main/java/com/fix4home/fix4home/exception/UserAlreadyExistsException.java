package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to create a user that already exists
 */
public class UserAlreadyExistsException extends BaseBusinessException {
    
    public UserAlreadyExistsException(String field, String value) {
        super(
            "USER_ALREADY_EXISTS",
            "User already exists with " + field + ": " + value,
            "An account with this " + field + " already exists. Please use a different " + field + ".",
            HttpStatus.CONFLICT
        );
    }
} 