package com.fix4home.fix4home.security;

import com.fix4home.fix4home.annotation.ApiVersion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for API versioning validation and deprecation warnings
 */
@Component
@Slf4j
public class ApiVersioningInterceptor implements HandlerInterceptor {
    
    private static final String VERSION_HEADER = "API-Version";
    private static final String DEPRECATION_HEADER = "API-Deprecation-Warning";
    private static final String SUPPORTED_VERSIONS_HEADER = "API-Supported-Versions";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        ApiVersion apiVersion = getApiVersion(handlerMethod);
        
        if (apiVersion == null) {
            return true; // No version annotation, proceed
        }
        
        // Get requested version from header or URL
        String requestedVersion = getRequestedVersion(request);
        
        // Validate version compatibility
        if (!isVersionSupported(requestedVersion, apiVersion)) {
            log.warn("Unsupported API version requested: {} for endpoint: {}", 
                requestedVersion, request.getRequestURI());
            
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setHeader("Content-Type", "application/json");
            response.getWriter().write(String.format(
                "{\"error\":\"Unsupported API version: %s. Supported versions: %s-%s\"}", 
                requestedVersion, apiVersion.minVersion(), 
                apiVersion.maxVersion().isEmpty() ? apiVersion.value() : apiVersion.maxVersion()));
            return false;
        }
        
        // Add version headers to response
        response.setHeader(VERSION_HEADER, apiVersion.value());
        response.setHeader(SUPPORTED_VERSIONS_HEADER, getSupportedVersionsString(apiVersion));
        
        // Add deprecation warning if applicable
        if (apiVersion.deprecated()) {
            response.setHeader(DEPRECATION_HEADER, 
                "This API version is deprecated. " + apiVersion.deprecationMessage());
            log.warn("Deprecated API version {} accessed for endpoint: {}", 
                apiVersion.value(), request.getRequestURI());
        }
        
        // Log API usage for monitoring
        log.info("API accessed: version={}, endpoint={}, method={}, userAgent={}", 
            requestedVersion, request.getRequestURI(), request.getMethod(), 
            request.getHeader("User-Agent"));
        
        return true;
    }
    
    /**
     * Get API version annotation from handler method or class
     */
    private ApiVersion getApiVersion(HandlerMethod handlerMethod) {
        // Check method-level annotation first
        ApiVersion methodVersion = handlerMethod.getMethodAnnotation(ApiVersion.class);
        if (methodVersion != null) {
            return methodVersion;
        }
        
        // Check class-level annotation
        return handlerMethod.getBeanType().getAnnotation(ApiVersion.class);
    }
    
    /**
     * Extract requested version from request
     */
    private String getRequestedVersion(HttpServletRequest request) {
        // Try header first
        String version = request.getHeader(VERSION_HEADER);
        if (version != null && !version.trim().isEmpty()) {
            return version.trim();
        }
        
        // Try query parameter
        version = request.getParameter("version");
        if (version != null && !version.trim().isEmpty()) {
            return version.trim();
        }
        
        // Extract from URL path (e.g., /api/v1/...)
        String uri = request.getRequestURI();
        if (uri.contains("/v")) {
            int vIndex = uri.indexOf("/v");
            int nextSlash = uri.indexOf("/", vIndex + 1);
            if (nextSlash > vIndex) {
                return uri.substring(vIndex + 1, nextSlash);
            } else {
                // Version might be at the end
                String[] parts = uri.substring(vIndex + 1).split("/");
                if (parts.length > 0) {
                    return parts[0];
                }
            }
        }
        
        // Default to v1
        return "v1";
    }
    
    /**
     * Check if requested version is supported
     */
    private boolean isVersionSupported(String requestedVersion, ApiVersion apiVersion) {
        if (requestedVersion == null || requestedVersion.trim().isEmpty()) {
            return true; // Default version is always supported
        }
        
        // Simple version comparison (assumes format like v1, v2, etc.)
        int requested = parseVersionNumber(requestedVersion);
        int min = parseVersionNumber(apiVersion.minVersion());
        int max = apiVersion.maxVersion().isEmpty() ? 
            parseVersionNumber(apiVersion.value()) : parseVersionNumber(apiVersion.maxVersion());
        
        return requested >= min && requested <= max;
    }
    
    /**
     * Parse version number from version string
     */
    private int parseVersionNumber(String version) {
        if (version == null || version.trim().isEmpty()) {
            return 1; // Default to v1
        }
        
        try {
            // Remove 'v' prefix if present
            String numberPart = version.toLowerCase().startsWith("v") ? 
                version.substring(1) : version;
            return Integer.parseInt(numberPart);
        } catch (NumberFormatException e) {
            log.warn("Could not parse version number: {}", version);
            return 1; // Default to v1
        }
    }
    
    /**
     * Get supported versions string for header
     */
    private String getSupportedVersionsString(ApiVersion apiVersion) {
        String max = apiVersion.maxVersion().isEmpty() ? apiVersion.value() : apiVersion.maxVersion();
        return apiVersion.minVersion() + "-" + max;
    }
}


