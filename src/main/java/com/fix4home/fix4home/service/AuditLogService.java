package com.fix4home.fix4home.service;

import com.fix4home.fix4home.model.entity.AuditLog;
import com.fix4home.fix4home.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for audit logging and security monitoring
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Log a request asynchronously for better performance
     */
    @Async
    public void logRequest(HttpServletRequest request, String action, String resource, 
                          Long resourceId, Object requestBody, Integer responseStatus, 
                          Long processingTime, Boolean success, String errorMessage) {
        try {
            AuditLog auditLog = buildAuditLog(request, action, resource, resourceId, 
                requestBody, responseStatus, processingTime, success, errorMessage);
            
            auditLogRepository.save(auditLog);
            
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }
    
    /**
     * Log a security event
     */
    @Async
    public void logSecurityEvent(String action, String details, String ipAddress, String userAgent) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            AuditLog auditLog = AuditLog.builder()
                .userId(auth != null && auth.isAuthenticated() ? getCurrentUserId() : null)
                .username(auth != null ? auth.getName() : "anonymous")
                .action("SECURITY_" + action)
                .resource("SECURITY")
                .method("SYSTEM")
                .endpoint("SECURITY_EVENT")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(false)
                .errorMessage(details)
                .additionalData(details)
                .build();
                
            auditLogRepository.save(auditLog);
            
        } catch (Exception e) {
            log.error("Failed to save security audit log", e);
        }
    }
    
    /**
     * Log user login attempt
     */
    @Async
    public void logLoginAttempt(String username, String ipAddress, String userAgent, boolean success, String errorMessage) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action(success ? "LOGIN_SUCCESS" : "LOGIN_FAILED")
                .resource("AUTH")
                .method("POST")
                .endpoint("/api/v1/auth/login")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(success)
                .errorMessage(errorMessage)
                .build();
                
            auditLogRepository.save(auditLog);
            
        } catch (Exception e) {
            log.error("Failed to save login audit log", e);
        }
    }
    
    /**
     * Check for suspicious activities
     */
    public boolean isSuspiciousActivity(String ipAddress) {
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        long failedAttempts = auditLogRepository.countFailedAttemptsByIp(ipAddress, since);
        
        // More than 10 failed attempts in 1 hour is suspicious
        return failedAttempts > 10;
    }
    
    /**
     * Get audit logs with pagination
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
    
    /**
     * Get audit logs by user
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * Get failed requests for security monitoring
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getFailedRequests(Pageable pageable) {
        return auditLogRepository.findFailedRequests(pageable);
    }
    
    /**
     * Get suspicious activities by IP
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getSuspiciousActivities(String ipAddress, int hoursBack) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        return auditLogRepository.findSuspiciousActivities(ipAddress, since);
    }
    
    /**
     * Cleanup old audit logs
     */
    @Transactional
    public void cleanupOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        auditLogRepository.deleteByCreatedAtBefore(cutoffDate);
        log.info("Cleaned up audit logs older than {} days", daysToKeep);
    }
    
    /**
     * Get user activity summary
     */
    @Transactional(readOnly = true)
    public List<Object[]> getUserActivitySummary(Long userId, int daysBack) {
        LocalDateTime since = LocalDateTime.now().minusDays(daysBack);
        return auditLogRepository.getUserActivitySummary(userId, since);
    }
    
    /**
     * Get most accessed endpoints
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMostAccessedEndpoints(int daysBack) {
        LocalDateTime since = LocalDateTime.now().minusDays(daysBack);
        return auditLogRepository.getMostAccessedEndpoints(since);
    }
    
    /**
     * Build audit log from request information
     */
    private AuditLog buildAuditLog(HttpServletRequest request, String action, String resource, 
                                  Long resourceId, Object requestBody, Integer responseStatus, 
                                  Long processingTime, Boolean success, String errorMessage) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        return AuditLog.builder()
            .userId(auth != null && auth.isAuthenticated() ? getCurrentUserId() : null)
            .username(auth != null ? auth.getName() : "anonymous")
            .action(action)
            .resource(resource)
            .resourceId(resourceId)
            .method(request.getMethod())
            .endpoint(request.getRequestURI())
            .ipAddress(getClientIpAddress(request))
            .userAgent(request.getHeader("User-Agent"))
            .requestBody(serializeRequestBody(requestBody))
            .responseStatus(responseStatus)
            .processingTime(processingTime)
            .sessionId(request.getSession(false) != null ? request.getSession().getId() : null)
            .success(success)
            .errorMessage(errorMessage)
            .build();
    }
    
    /**
     * Get client IP address considering proxies
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED"
        };
        
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Get first IP in case of comma-separated list
                return ip.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Serialize request body to JSON string
     */
    private String serializeRequestBody(Object requestBody) {
        if (requestBody == null) {
            return null;
        }
        
        try {
            // Limit size to prevent database issues
            String json = objectMapper.writeValueAsString(requestBody);
            if (json.length() > 5000) {
                return json.substring(0, 5000) + "... [TRUNCATED]";
            }
            return json;
        } catch (JsonProcessingException e) {
            log.debug("Could not serialize request body", e);
            return requestBody.toString();
        }
    }
    
    /**
     * Get current user ID from security context
     */
    private Long getCurrentUserId() {
        // This would need to be implemented based on your user details implementation
        // For now, return null - can be enhanced later
        return null;
    }
}
