package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.entity.AuditLog;
import com.fix4home.fix4home.security.SecurityConstants;
import com.fix4home.fix4home.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for audit log management and security monitoring
 */
@RestController
@RequestMapping("/api/v1/admin/audit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Audit Controller", description = "Audit log management and security monitoring APIs for administrators")
public class AdminAuditController {
    
    private final AuditLogService auditLogService;
    
    @GetMapping("/logs")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get all audit logs", 
               description = "Get paginated audit logs for security monitoring")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogs(Pageable pageable) {
        log.info("Admin getting audit logs with pagination");
        
        Page<AuditLog> auditLogs = auditLogService.getAuditLogs(pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success("Audit logs retrieved successfully", auditLogs));
    }
    
    @GetMapping("/logs/user/{userId}")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get audit logs by user", 
               description = "Get audit logs for a specific user")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogsByUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            Pageable pageable) {
        log.info("Admin getting audit logs for user ID: {}", userId);
        
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsByUser(userId, pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success("User audit logs retrieved successfully", auditLogs));
    }
    
    @GetMapping("/logs/failed")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get failed requests", 
               description = "Get failed requests for security monitoring")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getFailedRequests(Pageable pageable) {
        log.info("Admin getting failed requests for security monitoring");
        
        Page<AuditLog> failedLogs = auditLogService.getFailedRequests(pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success("Failed requests retrieved successfully", failedLogs));
    }
    
    @GetMapping("/logs/suspicious/{ipAddress}")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get suspicious activities", 
               description = "Get suspicious activities for a specific IP address")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getSuspiciousActivities(
            @Parameter(description = "IP Address") @PathVariable String ipAddress,
            @Parameter(description = "Hours back to search") @RequestParam(defaultValue = "24") int hoursBack) {
        log.info("Admin getting suspicious activities for IP: {} in last {} hours", ipAddress, hoursBack);
        
        List<AuditLog> suspiciousLogs = auditLogService.getSuspiciousActivities(ipAddress, hoursBack);
        
        return ResponseEntity.ok(
                ApiResponse.success("Suspicious activities retrieved successfully", suspiciousLogs));
    }
    
    @GetMapping("/logs/user/{userId}/activity-summary")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get user activity summary", 
               description = "Get activity summary for a specific user")
    public ResponseEntity<ApiResponse<List<Object[]>>> getUserActivitySummary(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Days back to analyze") @RequestParam(defaultValue = "30") int daysBack) {
        log.info("Admin getting activity summary for user ID: {} for last {} days", userId, daysBack);
        
        List<Object[]> activitySummary = auditLogService.getUserActivitySummary(userId, daysBack);
        
        return ResponseEntity.ok(
                ApiResponse.success("User activity summary retrieved successfully", activitySummary));
    }
    
    @GetMapping("/logs/endpoints/popular")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get most accessed endpoints", 
               description = "Get most accessed endpoints for system monitoring")
    public ResponseEntity<ApiResponse<List<Object[]>>> getMostAccessedEndpoints(
            @Parameter(description = "Days back to analyze") @RequestParam(defaultValue = "7") int daysBack) {
        log.info("Admin getting most accessed endpoints for last {} days", daysBack);
        
        List<Object[]> endpointStats = auditLogService.getMostAccessedEndpoints(daysBack);
        
        return ResponseEntity.ok(
                ApiResponse.success("Endpoint statistics retrieved successfully", endpointStats));
    }
    
    @PostMapping("/logs/cleanup")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Cleanup old audit logs", 
               description = "Remove audit logs older than specified days")
    public ResponseEntity<ApiResponse<String>> cleanupOldLogs(
            @Parameter(description = "Days to keep") @RequestParam(defaultValue = "90") int daysToKeep) {
        log.info("Admin initiating cleanup of audit logs older than {} days", daysToKeep);
        
        auditLogService.cleanupOldLogs(daysToKeep);
        
        return ResponseEntity.ok(
                ApiResponse.success("Old audit logs cleaned up successfully", 
                    "Removed logs older than " + daysToKeep + " days"));
    }
    
    @GetMapping("/security/suspicious-check/{ipAddress}")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Check if IP is suspicious", 
               description = "Check if an IP address has suspicious activity patterns")
    public ResponseEntity<ApiResponse<Boolean>> checkSuspiciousActivity(
            @Parameter(description = "IP Address") @PathVariable String ipAddress) {
        log.info("Admin checking if IP {} has suspicious activity", ipAddress);
        
        boolean isSuspicious = auditLogService.isSuspiciousActivity(ipAddress);
        
        return ResponseEntity.ok(
                ApiResponse.success("Suspicious activity check completed", isSuspicious));
    }
}






