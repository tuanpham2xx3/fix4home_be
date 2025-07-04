package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user is not found in the system
 */
public class UserNotFoundException extends BaseBusinessException {
    
    public UserNotFoundException(String identifier) {
        super(
            "USER_NOT_FOUND",
            "User not found with identifier: " + identifier,
            "User account not found. Please check your credentials.",
            HttpStatus.NOT_FOUND
        );
    }
    
    public UserNotFoundException(Long userId) {
        super(
            "USER_NOT_FOUND",
            "User not found with ID: " + userId,
            "User account not found.",
            HttpStatus.NOT_FOUND
        );
    }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Create exception for user not found by ID
     */
    public static UserNotFoundException withId(Long userId) {
        return new UserNotFoundException(userId);
    }

    /**
     * Create exception for user not found by username
     */
    public static UserNotFoundException withUsername(String username) {
        return new UserNotFoundException(username);
    }

    /**
     * Create exception for current authenticated user not found
     */
    public static UserNotFoundException currentUserNotFound() {
        return new UserNotFoundException("current authenticated user");
    }

    /**
     * Create exception for user not found with custom message
     */
    public static UserNotFoundException withMessage(String message) {
        return new UserNotFoundException(message);
    }
} 