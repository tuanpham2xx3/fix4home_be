package com.fix4home.fix4home.security;

import com.fix4home.fix4home.service.AuditLogService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Filter for logging HTTP requests and responses for audit purposes
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLoggingFilter implements Filter {
    
    private final AuditLogService auditLogService;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Skip logging for certain endpoints
        if (shouldSkipLogging(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }
        
        // Wrap request and response to capture content
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);
        
        long startTime = System.currentTimeMillis();
        
        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Log the request asynchronously
            logRequest(wrappedRequest, wrappedResponse, processingTime);
            
            // Don't forget to copy the response body back
            wrappedResponse.copyBodyToResponse();
        }
    }
    
    /**
     * Log the request details
     */
    private void logRequest(ContentCachingRequestWrapper request, 
                           ContentCachingResponseWrapper response, 
                           long processingTime) {
        try {
            String action = determineAction(request);
            String resource = determineResource(request);
            String requestBody = getRequestBody(request);
            boolean success = response.getStatus() < 400;
            String errorMessage = success ? null : "HTTP " + response.getStatus();
            
            auditLogService.logRequest(
                request, 
                action, 
                resource, 
                null, // resourceId can be extracted from path if needed
                requestBody, 
                response.getStatus(), 
                processingTime, 
                success, 
                errorMessage
            );
            
        } catch (Exception e) {
            log.error("Error logging request", e);
        }
    }
    
    /**
     * Determine the action being performed
     */
    private String determineAction(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        
        // Map HTTP methods to actions
        switch (method.toUpperCase()) {
            case "GET":
                return uri.contains("/my") || uri.contains("/profile") ? "VIEW_OWN" : "VIEW";
            case "POST":
                if (uri.contains("/auth/login")) return "LOGIN";
                if (uri.contains("/auth/register")) return "REGISTER";
                if (uri.contains("/auth/refresh")) return "REFRESH_TOKEN";
                return "CREATE";
            case "PUT":
                return "UPDATE";
            case "DELETE":
                return "DELETE";
            case "PATCH":
                return "PARTIAL_UPDATE";
            default:
                return method.toUpperCase();
        }
    }
    
    /**
     * Determine the resource being accessed
     */
    private String determineResource(HttpServletRequest request) {
        String uri = request.getRequestURI();
        
        if (uri.startsWith("/api/v1/")) {
            String[] parts = uri.substring(8).split("/"); // Remove "/api/v1/"
            if (parts.length > 0) {
                return parts[0].toUpperCase();
            }
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Extract request body content
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            // Truncate if too long and mask sensitive data
            return maskSensitiveData(body);
        }
        return null;
    }
    
    /**
     * Mask sensitive data in request body
     */
    private String maskSensitiveData(String body) {
        if (body == null) {
            return null;
        }
        
        // Mask passwords, tokens, and other sensitive fields
        String masked = body
            .replaceAll("(\"password\"\\s*:\\s*\")[^\"]*(\")","$1***MASKED***$2")
            .replaceAll("(\"token\"\\s*:\\s*\")[^\"]*(\")","$1***MASKED***$2")
            .replaceAll("(\"refreshToken\"\\s*:\\s*\")[^\"]*(\")","$1***MASKED***$2")
            .replaceAll("(\"cardNumber\"\\s*:\\s*\")[^\"]*(\")","$1***MASKED***$2")
            .replaceAll("(\"cvv\"\\s*:\\s*\")[^\"]*(\")","$1***MASKED***$2");
        
        // Limit length
        if (masked.length() > 2000) {
            return masked.substring(0, 2000) + "... [TRUNCATED]";
        }
        
        return masked;
    }
    
    /**
     * Check if logging should be skipped for this endpoint
     */
    private boolean shouldSkipLogging(String uri) {
        return uri.startsWith("/swagger-ui") ||
               uri.startsWith("/api-docs") ||
               uri.startsWith("/actuator/health") ||
               uri.startsWith("/favicon.ico") ||
               uri.endsWith(".css") ||
               uri.endsWith(".js") ||
               uri.endsWith(".png") ||
               uri.endsWith(".jpg") ||
               uri.endsWith(".ico");
    }
}





