package com.fix4home.fix4home.model.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Enhanced error response with error codes and detailed information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private boolean success;
    private String errorCode;
    private String message;
    private String userMessage;
    private Object details;
    private LocalDateTime timestamp;
    private String path;
    private int status;
    
    // Validation errors specific fields
    private Map<String, String> fieldErrors;
    
    public static ErrorResponse business(String errorCode, String message, String userMessage, int status) {
        return ErrorResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .userMessage(userMessage)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse validation(String message, Map<String, String> fieldErrors) {
        return ErrorResponse.builder()
                .success(false)
                .errorCode("VALIDATION_FAILED")
                .message("Validation failed")
                .userMessage(message)
                .fieldErrors(fieldErrors)
                .status(400)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse generic(String message, int status) {
        return ErrorResponse.builder()
                .success(false)
                .errorCode("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .userMessage(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public ErrorResponse withPath(String path) {
        this.path = path;
        return this;
    }
    
    public ErrorResponse withDetails(Object details) {
        this.details = details;
        return this;
    }
} 