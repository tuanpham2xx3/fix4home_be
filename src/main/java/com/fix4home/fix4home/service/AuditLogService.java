package com.fix4home.fix4home.service;

import com.fix4home.fix4home.model.entity.AuditLog;
import com.fix4home.fix4home.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for audit log operations and security monitoring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    
    private static final int SUSPICIOUS_THRESHOLD = 5; // Failed attempts threshold
    
    /**
     * Get all audit logs with pagination
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        log.debug("Getting audit logs with pagination: {}", pageable);
        return auditLogRepository.findAll(pageable);
    }
    
    /**
     * Get audit logs by user ID
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByUser(Long userId, Pageable pageable) {
        log.debug("Getting audit logs for user ID: {}", userId);
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * Get failed requests for security monitoring
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getFailedRequests(Pageable pageable) {
        log.debug("Getting failed requests for security monitoring");
        return auditLogRepository.findFailedRequests(pageable);
    }
    
    /**
     * Get suspicious activities for an IP address
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getSuspiciousActivities(String ipAddress, int hoursBack) {
        log.debug("Getting suspicious activities for IP: {} in last {} hours", ipAddress, hoursBack);
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        return auditLogRepository.findSuspiciousActivities(ipAddress, since);
    }
    
    /**
     * Get user activity summary
     */
    @Transactional(readOnly = true)
    public List<Object[]> getUserActivitySummary(Long userId, int daysBack) {
        log.debug("Getting activity summary for user ID: {} for last {} days", userId, daysBack);
        LocalDateTime since = LocalDateTime.now().minusDays(daysBack);
        return auditLogRepository.getUserActivitySummary(userId, since);
    }
    
    /**
     * Get most accessed endpoints
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMostAccessedEndpoints(int daysBack) {
        log.debug("Getting most accessed endpoints for last {} days", daysBack);
        LocalDateTime since = LocalDateTime.now().minusDays(daysBack);
        return auditLogRepository.getMostAccessedEndpoints(since);
    }
    
    /**
     * Cleanup old audit logs
     */
    @Transactional
    public void cleanupOldLogs(int daysToKeep) {
        log.info("Cleaning up audit logs older than {} days", daysToKeep);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        auditLogRepository.deleteByCreatedAtBefore(cutoffDate);
    }
    
    /**
     * Check if IP address has suspicious activity
     */
    @Transactional(readOnly = true)
    public boolean isSuspiciousActivity(String ipAddress) {
        log.debug("Checking if IP {} has suspicious activity", ipAddress);
        LocalDateTime since = LocalDateTime.now().minusHours(1); // Check last hour
        long failedAttempts = auditLogRepository.countFailedAttemptsByIp(ipAddress, since);
        return failedAttempts >= SUSPICIOUS_THRESHOLD;
    }
    
    /**
     * Save audit log entry
     */
    @Transactional
    public AuditLog saveAuditLog(AuditLog auditLog) {
        log.debug("Saving audit log entry for action: {}", auditLog.getAction());
        return auditLogRepository.save(auditLog);
    }
    
    /**
     * Log HTTP request for audit purposes
     */
    @Transactional
    public void logRequest(jakarta.servlet.http.HttpServletRequest request, 
                          String action, 
                          String resource, 
                          Long resourceId, 
                          String requestBody, 
                          int responseStatus, 
                          long processingTime, 
                          boolean success, 
                          String errorMessage) {
        
        AuditLog auditLog = AuditLog.builder()
            .action(action)
            .resource(resource)
            .resourceId(resourceId)
            .method(request.getMethod())
            .endpoint(request.getRequestURI())
            .ipAddress(getClientIpAddress(request))
            .userAgent(request.getHeader("User-Agent"))
            .requestBody(requestBody)
            .responseStatus(responseStatus)
            .processingTime(processingTime)
            .sessionId(request.getSession(false) != null ? request.getSession().getId() : null)
            .success(success)
            .errorMessage(errorMessage)
            .build();
            
        // Extract user info if available
        if (request.getUserPrincipal() != null) {
            auditLog.setUsername(request.getUserPrincipal().getName());
        }
        
        auditLogRepository.save(auditLog);
    }
    
    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(jakarta.servlet.http.HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
