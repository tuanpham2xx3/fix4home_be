package com.fix4home.fix4home.exception;

import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.dto.common.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ==================== BUSINESS EXCEPTIONS ====================

    @ExceptionHandler(BaseBusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BaseBusinessException ex, WebRequest request) {
        log.error("Business exception [{}]: {}", ex.getErrorCode(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.business(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getUserMessage(),
                ex.getHttpStatus().value()
        ).withPath(getPath(request))
         .withDetails(ex.getDetails());
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    // ==================== LEGACY EXCEPTIONS (for backward compatibility) ====================

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        log.error("Resource already exists: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(BadRequestException ex) {
        log.error("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ==================== SECURITY EXCEPTIONS ====================

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(UsernameNotFoundException ex, WebRequest request) {
        log.error("Username not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.business(
                "USER_NOT_FOUND",
                "User not found: " + ex.getMessage(),
                "User account not found. Please check your credentials.",
                404
        ).withPath(getPath(request));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        log.error("Bad credentials: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.business(
                "INVALID_CREDENTIALS",
                "Bad credentials: " + ex.getMessage(),
                "Invalid username or password. Please try again.",
                401
        ).withPath(getPath(request));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.error("Access denied: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.business(
                "ACCESS_DENIED",
                "Access denied: " + ex.getMessage(),
                "You don't have permission to perform this action.",
                403
        ).withPath(getPath(request));
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    // ==================== VALIDATION EXCEPTIONS ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        log.error("Validation failed: {}", fieldErrors);
        
        ErrorResponse errorResponse = ErrorResponse.validation(
                "Please correct the validation errors and try again.",
                fieldErrors
        ).withPath(getPath(request));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // ==================== GENERIC EXCEPTION ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: ", ex);
        
        ErrorResponse errorResponse = ErrorResponse.generic(
                "An unexpected error occurred. Please try again later.",
                500
        ).withPath(getPath(request));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // ==================== UTILITY METHODS ====================

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
} 