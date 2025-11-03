package com.fix4home.fix4home.exception;

import com.fix4home.fix4home.model.enums.ComplaintStatus;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a complaint resolution operation fails
 */
public class ComplaintResolutionException extends BaseBusinessException {
    
    public ComplaintResolutionException(String message) {
        super(
            "COMPLAINT_RESOLUTION_FAILED",
            "Complaint resolution failed: " + message,
            message,
            HttpStatus.BAD_REQUEST
        );
    }
    
    public ComplaintResolutionException(ComplaintStatus currentStatus, String operation) {
        super(
            "COMPLAINT_RESOLUTION_FAILED",
            "Cannot " + operation + " complaint in " + currentStatus + " status",
            "This operation cannot be performed on a complaint in " + currentStatus.name().toLowerCase() + " status.",
            HttpStatus.BAD_REQUEST
        );
    }
    
    // Static factory methods
    public static ComplaintResolutionException cannotInvestigate(ComplaintStatus currentStatus) {
        return new ComplaintResolutionException(currentStatus, "investigate");
    }
    
    public static ComplaintResolutionException cannotResolve(ComplaintStatus currentStatus) {
        return new ComplaintResolutionException(currentStatus, "resolve");
    }
    
    public static ComplaintResolutionException cannotModify(ComplaintStatus currentStatus) {
        return new ComplaintResolutionException(currentStatus, "modify");
    }
    
    public static ComplaintResolutionException alreadyResolved() {
        return new ComplaintResolutionException("Complaint has already been resolved and cannot be modified.");
    }
    
    public static ComplaintResolutionException cannotComplainAgainstSelf() {
        return new ComplaintResolutionException("You cannot file a complaint against yourself.");
    }
    
    public static ComplaintResolutionException serviceRequestNotCompleted() {
        return new ComplaintResolutionException("Complaints can only be filed for completed or done service requests.");
    }
    
    public static ComplaintResolutionException withMessage(String message) {
        return new ComplaintResolutionException(message);
    }
} 