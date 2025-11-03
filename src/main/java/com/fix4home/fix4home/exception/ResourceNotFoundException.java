package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;

/**
 * Generic exception for when any resource is not found
 */
public class ResourceNotFoundException extends BaseBusinessException {
    
    public ResourceNotFoundException(String resource, String identifier) {
        super(
            "RESOURCE_NOT_FOUND",
            resource + " not found with identifier: " + identifier,
            "The requested resource was not found.",
            HttpStatus.NOT_FOUND
        );
    }
    
    public ResourceNotFoundException(String message) {
        super(
            "RESOURCE_NOT_FOUND",
            message,
            "The requested resource was not found.",
            HttpStatus.NOT_FOUND
        );
    }
    
    // Static factory methods for common resources
    public static ResourceNotFoundException file(String filename) {
        return new ResourceNotFoundException("File", filename);
    }
    
    public static ResourceNotFoundException fileWithId(Long fileId) {
        return new ResourceNotFoundException("File", fileId.toString());
    }
    
    public static ResourceNotFoundException withMessage(String message) {
        return new ResourceNotFoundException(message);
    }
}
