package com.fix4home.fix4home.exception;
 
public class TechnicianProfileNotFoundException extends BusinessValidationException {
    public TechnicianProfileNotFoundException(Long userId) {
        super("Technician profile not found for user: " + userId);
    }
} 