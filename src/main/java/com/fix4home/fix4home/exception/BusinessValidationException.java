package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for business logic validation failures
 */
public class BusinessValidationException extends BaseBusinessException {
    
    public BusinessValidationException(String field, String message) {
        super(
            "BUSINESS_VALIDATION_FAILED",
            "Validation failed for field '" + field + "': " + message,
            message,
            HttpStatus.BAD_REQUEST
        );
    }
    
    public BusinessValidationException(String message) {
        super(
            "BUSINESS_VALIDATION_FAILED",
            "Business validation failed: " + message,
            message,
            HttpStatus.BAD_REQUEST
        );
    }
    
    public static BusinessValidationException invalidDateRange() {
        return new BusinessValidationException("End date must be after start date");
    }
    
    public static BusinessValidationException invalidRating(int rating) {
        return new BusinessValidationException("rating", "Rating must be between 1 and 5, got: " + rating);
    }
    
    public static BusinessValidationException pastDate(String fieldName) {
        return new BusinessValidationException(fieldName, "Date cannot be in the past");
    }
    
    public static BusinessValidationException requiredField(String fieldName) {
        return new BusinessValidationException(fieldName, "This field is required");
    }
} 