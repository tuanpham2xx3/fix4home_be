package com.fix4home.fix4home.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base class for all business-specific exceptions in Fix4Home application.
 * Provides common error handling structure with error codes, HTTP status, and detailed messages.
 */
@Getter
public abstract class BaseBusinessException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final String userMessage;
    private final Object details;
    
    public BaseBusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.userMessage = message;
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.details = null;
    }
    
    public BaseBusinessException(String errorCode, String message, String userMessage, HttpStatus httpStatus) {
        this(errorCode, message, userMessage, httpStatus, null);
    }
    
    protected BaseBusinessException(String errorCode, String message, String userMessage, HttpStatus httpStatus, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.httpStatus = httpStatus;
        this.details = details;
    }
    
    protected BaseBusinessException(String errorCode, String message, String userMessage, HttpStatus httpStatus, Throwable cause) {
        this(errorCode, message, userMessage, httpStatus, null, cause);
    }
    
    protected BaseBusinessException(String errorCode, String message, String userMessage, HttpStatus httpStatus, Object details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.httpStatus = httpStatus;
        this.details = details;
    }
} 