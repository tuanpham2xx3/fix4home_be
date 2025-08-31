package com.fix4home.fix4home.security;

import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helper class for security-related operations
 */
public final class SecurityHelper {
    
    private SecurityHelper() {
        throw new AssertionError("SecurityHelper should not be instantiated");
    }
    
    /**
     * Get current authenticated user from security context
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        }
        return null;
    }
    
    /**
     * Get current authenticated username
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return null;
    }
    
    /**
     * Check if current user has admin role
     */
    public static boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }
    
    /**
     * Check if current user has customer role
     */
    public static boolean isCustomer(User user) {
        return user != null && user.getRole() == Role.CUSTOMER;
    }
    
    /**
     * Check if current user has technician role
     */
    public static boolean isTechnician(User user) {
        return user != null && user.getRole() == Role.TECHNICIAN;
    }
    
    /**
     * Check if current authenticated user is admin
     */
    public static boolean isCurrentUserAdmin() {
        User currentUser = getCurrentUser();
        return isAdmin(currentUser);
    }
    
    /**
     * Check if current authenticated user is customer
     */
    public static boolean isCurrentUserCustomer() {
        User currentUser = getCurrentUser();
        return isCustomer(currentUser);
    }
    
    /**
     * Check if current authenticated user is technician
     */
    public static boolean isCurrentUserTechnician() {
        User currentUser = getCurrentUser();
        return isTechnician(currentUser);
    }
}
