package com.fix4home.fix4home.security;

/**
 * Security constants for authorization and role management
 * Centralizes all security-related string constants to ensure consistency
 * and easier maintenance across the application.
 */
public final class SecurityConstants {
    
    // Private constructor to prevent instantiation
    private SecurityConstants() {
        throw new AssertionError("SecurityConstants should not be instantiated");
    }
    
    // ==================== ROLE CONSTANTS ====================
    
    /**
     * Role constant for Admin users
     */
    public static final String ROLE_ADMIN = "ADMIN";
    
    /**
     * Role constant for Customer users
     */
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    
    /**
     * Role constant for Technician users
     */
    public static final String ROLE_TECHNICIAN = "TECHNICIAN";
    
    // ==================== SINGLE ROLE AUTHORIZATION PATTERNS ====================
    
    /**
     * Authorization expression for Admin-only access
     */
    public static final String HAS_ADMIN_ROLE = "hasRole('" + ROLE_ADMIN + "')";
    
    /**
     * Authorization expression for Customer-only access
     */
    public static final String HAS_CUSTOMER_ROLE = "hasRole('" + ROLE_CUSTOMER + "')";
    
    /**
     * Authorization expression for Technician-only access
     */
    public static final String HAS_TECHNICIAN_ROLE = "hasRole('" + ROLE_TECHNICIAN + "')";
    
    // ==================== MULTIPLE ROLE AUTHORIZATION PATTERNS ====================
    
    /**
     * Authorization expression for any authenticated user (Customer, Technician, or Admin)
     */
    public static final String HAS_ANY_ROLE = "hasAnyRole('" + ROLE_CUSTOMER + "', '" + ROLE_TECHNICIAN + "', '" + ROLE_ADMIN + "')";
    
    /**
     * Authorization expression for Customer or Admin access
     */
    public static final String HAS_CUSTOMER_OR_ADMIN_ROLE = "hasAnyRole('" + ROLE_CUSTOMER + "', '" + ROLE_ADMIN + "')";
    
    /**
     * Authorization expression for Technician or Admin access
     */
    public static final String HAS_TECHNICIAN_OR_ADMIN_ROLE = "hasAnyRole('" + ROLE_TECHNICIAN + "', '" + ROLE_ADMIN + "')";
    
    /**
     * Authorization expression for Customer or Technician access
     */
    public static final String HAS_CUSTOMER_OR_TECHNICIAN_ROLE = "hasAnyRole('" + ROLE_CUSTOMER + "', '" + ROLE_TECHNICIAN + "')";
    
    // ==================== JWT CONSTANTS ====================
    
    /**
     * JWT token prefix used in Authorization header
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * Authorization header name
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Creates a custom hasRole expression for a specific role
     * @param role the role name
     * @return hasRole expression string
     */
    public static String hasRole(String role) {
        return "hasRole('" + role + "')";
    }
    
    /**
     * Creates a custom hasAnyRole expression for multiple roles
     * @param roles array of role names
     * @return hasAnyRole expression string
     */
    public static String hasAnyRole(String... roles) {
        if (roles == null || roles.length == 0) {
            throw new IllegalArgumentException("At least one role must be provided");
        }
        
        StringBuilder sb = new StringBuilder("hasAnyRole(");
        for (int i = 0; i < roles.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("'").append(roles[i]).append("'");
        }
        sb.append(")");
        return sb.toString();
    }
} 