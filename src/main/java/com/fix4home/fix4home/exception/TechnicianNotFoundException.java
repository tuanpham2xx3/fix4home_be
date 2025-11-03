package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a technician is not found
 */
public class TechnicianNotFoundException extends BaseBusinessException {
    
    public TechnicianNotFoundException(Long technicianId) {
        super(
            "TECHNICIAN_NOT_FOUND",
            "Technician not found with ID: " + technicianId,
            "The requested technician was not found.",
            HttpStatus.NOT_FOUND
        );
    }
    
    public TechnicianNotFoundException(Long userId, String context) {
        super(
            "TECHNICIAN_NOT_FOUND",
            "Technician not found for user ID: " + userId + " (" + context + ")",
            "Technician profile not found.",
            HttpStatus.NOT_FOUND
        );
    }
} 