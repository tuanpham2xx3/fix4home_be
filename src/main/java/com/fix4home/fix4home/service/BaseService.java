package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.BusinessValidationException;
import com.fix4home.fix4home.exception.UserNotFoundException;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Base service class providing common functionality for all services
 * Includes authentication, authorization, and common validation patterns
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseService {

    @Autowired
    protected UserRepository userRepository;

    // ==================== AUTHENTICATION UTILITIES ====================

    /**
     * Get current authenticated user
     */
    protected User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> UserNotFoundException.currentUserNotFound());
    }

    /**
     * Get current authenticated user ID
     */
    protected Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Get current authenticated username
     */
    protected String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    /**
     * Check if current user has specific role
     */
    protected boolean hasRole(Role role) {
        return getCurrentUser().getRole() == role;
    }

    /**
     * Require current user to have specific role
     */
    protected void requireRole(Role role) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != role) {
            throw new SecurityException("Access denied: Role " + role + " required");
        }
    }

    /**
     * Require current user to have one of the specified roles
     */
    protected void requireAnyRole(Role... roles) {
        User currentUser = getCurrentUser();
        for (Role role : roles) {
            if (currentUser.getRole() == role) {
                return;
            }
        }
        throw new SecurityException("Access denied: One of the following roles required: " + java.util.Arrays.toString(roles));
    }

    // ==================== USER VALIDATION UTILITIES ====================

    /**
     * Find user by ID with validation
     */
    protected User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.withId(userId));
    }

    /**
     * Find user by username with validation
     */
    protected User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> UserNotFoundException.withUsername(username));
    }

    /**
     * Validate user has specific role
     */
    protected void validateUserRole(User user, Role expectedRole) {
        if (user.getRole() != expectedRole) {
            throw new SecurityException("User is not a " + expectedRole.name().toLowerCase());
        }
    }

    /**
     * Check if current user can access resource owned by specific user
     */
    protected boolean canAccessUserResource(Long resourceOwnerId) {
        User currentUser = getCurrentUser();
        
        // Admin can access everything
        if (currentUser.getRole() == Role.ADMIN) {
            return true;
        }
        
        // Users can access their own resources
        return currentUser.getId().equals(resourceOwnerId);
    }

    /**
     * Require current user to have access to resource owned by specific user
     */
    protected void requireAccessToUserResource(Long resourceOwnerId) {
        if (!canAccessUserResource(resourceOwnerId)) {
            throw new SecurityException("Access denied: You can only access your own resources");
        }
    }

    // ==================== LOGGING UTILITIES ====================

    /**
     * Log service method entry with parameters
     */
    protected void logMethodEntry(String methodName, Object... params) {
        if (log.isDebugEnabled()) {
            log.debug("Entering {}.{} with params: {}", 
                     this.getClass().getSimpleName(), methodName, java.util.Arrays.toString(params));
        }
    }

    /**
     * Log service method exit with result
     */
    protected void logMethodExit(String methodName, Object result) {
        if (log.isDebugEnabled()) {
            log.debug("Exiting {}.{} with result type: {}", 
                     this.getClass().getSimpleName(), methodName, 
                     result != null ? result.getClass().getSimpleName() : "null");
        }
    }

    /**
     * Log business operation
     */
    protected void logBusinessOperation(String operation, Object... context) {
        log.info("Business operation: {} | Context: {}", operation, java.util.Arrays.toString(context));
    }

    // ==================== VALIDATION UTILITIES ====================

    /**
     * Validate required parameter is not null
     */
    protected void validateRequired(Object parameter, String parameterName) {
        if (parameter == null) {
            throw new BusinessValidationException(parameterName + " cannot be null");
        }
    }

    /**
     * Validate required string parameter is not null or empty
     */
    protected void validateRequired(String parameter, String parameterName) {
        if (parameter == null || parameter.trim().isEmpty()) {
            throw new BusinessValidationException(parameterName + " cannot be null or empty");
        }
    }

    /**
     * Validate ID parameter is positive
     */
    protected void validatePositiveId(Long id, String parameterName) {
        if (id == null || id <= 0) {
            throw new BusinessValidationException(parameterName + " must be a positive number");
        }
    }

    /**
     * Validate number is positive
     */
    protected void validatePositive(int value, String parameterName) {
        if (value <= 0) {
            throw new BusinessValidationException(parameterName + " must be a positive number");
        }
    }

    /**
     * Validate pagination parameters
     */
    protected void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new BusinessValidationException("Page number cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessValidationException("Page size must be between 1 and 100");
        }
    }

    /**
     * Validate sort direction
     */
    protected void validateSortDirection(String sortDir) {
        if (!"asc".equalsIgnoreCase(sortDir) && !"desc".equalsIgnoreCase(sortDir)) {
            throw new BusinessValidationException("Sort direction must be 'asc' or 'desc'");
        }
    }
} 