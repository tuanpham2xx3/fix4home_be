package com.fix4home.fix4home.exception;

import com.fix4home.fix4home.model.enums.UserStatus;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a technician is not available for assignment
 */
public class TechnicianNotAvailableException extends BaseBusinessException {
    
    public TechnicianNotAvailableException(String message) {
        super(
            "TECHNICIAN_NOT_AVAILABLE",
            message,
            "The technician is not available for assignment.",
            HttpStatus.BAD_REQUEST
        );
    }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Create exception for technician not available due to status
     */
    public static TechnicianNotAvailableException withStatus(UserStatus status) {
        return new TechnicianNotAvailableException(
            "Technician is not available. Current status: " + status
        );
    }

    /**
     * Create exception for technician not available by ID
     */
    public static TechnicianNotAvailableException withId(Long technicianId) {
        return new TechnicianNotAvailableException(
            "Technician not available with ID: " + technicianId
        );
    }

    /**
     * Create exception for busy technician
     */
    public static TechnicianNotAvailableException busy(Long technicianId) {
        return new TechnicianNotAvailableException(
            "Technician is currently busy with ID: " + technicianId
        );
    }

    /**
     * Create exception for unapproved technician
     */
    public static TechnicianNotAvailableException notApproved(Long technicianId) {
        return new TechnicianNotAvailableException(
            "Technician is not approved for assignments with ID: " + technicianId
        );
    }
} 