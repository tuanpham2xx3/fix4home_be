package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.admin.*;
import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Controller", description = "System administration APIs for managing the Fix4Home platform")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ==================== DASHBOARD & OVERVIEW ====================

    @GetMapping("/dashboard")
    @Operation(summary = "Get system overview", 
               description = "Admin dashboard with comprehensive system statistics and metrics")
    public ResponseEntity<ApiResponse<SystemOverviewDTO>> getDashboardOverview() {
        log.info("Admin requesting system overview");
        
        SystemOverviewDTO overview = adminService.getSystemOverview();
        
        return ResponseEntity.ok(
                ApiResponse.success("System overview retrieved successfully", overview));
    }

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/users")
    @Operation(summary = "Get all users", 
               description = "Admin views all users with pagination, sorting, and role filtering")
    public ResponseEntity<ApiResponse<Page<UserManagementDTO>>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filter by role") @RequestParam(required = false) Role role) {
        log.info("Admin fetching all users - page: {}, size: {}, role: {}", page, size, role);
        
        Page<UserManagementDTO> users = adminService.getAllUsers(page, size, sortBy, sortDir, role);
        
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user details", 
               description = "Admin views detailed information about a specific user")
    public ResponseEntity<ApiResponse<UserManagementDTO>> getUserById(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        log.info("Admin fetching user details for ID: {}", userId);
        
        UserManagementDTO user = adminService.getUserById(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success("User details retrieved successfully", user));
    }

    @PutMapping("/users/{userId}/status")
    @Operation(summary = "Update user status", 
               description = "Admin updates a user's status (ACTIVE, INACTIVE, SUSPENDED)")
    public ResponseEntity<ApiResponse<UserManagementDTO>> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        log.info("Admin updating user status for ID: {} to {}", userId, request.getStatus());
        
        UserManagementDTO user = adminService.updateUserStatus(userId, request);
        
        return ResponseEntity.ok(
                ApiResponse.success("User status updated successfully", user));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user", 
               description = "Admin deletes a user account (permanent action)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        log.info("Admin deleting user with ID: {}", userId);
        
        adminService.deleteUser(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully", null));
    }

    // ==================== BULK OPERATIONS ====================

    @PostMapping("/bulk-operations")
    @Operation(summary = "Execute bulk operations", 
               description = "Admin performs bulk operations on multiple users/services/requests")
    public ResponseEntity<ApiResponse<BulkOperationResultDTO>> executeBulkOperation(
            @Valid @RequestBody BulkOperationRequest request) {
        log.info("Admin executing bulk operation: {} for {} targets", 
                 request.getOperationType(), request.getTargetIds().size());
        
        BulkOperationResultDTO result = adminService.executeBulkOperation(request);
        
        return ResponseEntity.ok(
                ApiResponse.success("Bulk operation executed successfully", result));
    }

    // ==================== REPORTING ====================

    @GetMapping("/reports/system")
    @Operation(summary = "Generate system report", 
               description = "Admin generates comprehensive system reports for specified date range")
    public ResponseEntity<ApiResponse<SystemReportDTO>> generateSystemReport(
            @Parameter(description = "Report type") @RequestParam(defaultValue = "MONTHLY") String reportType,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Admin generating system report: {} from {} to {}", reportType, startDate, endDate);
        
        SystemReportDTO report = adminService.generateSystemReport(reportType, startDate, endDate);
        
        return ResponseEntity.ok(
                ApiResponse.success("System report generated successfully", report));
    }

    @GetMapping("/reports/users")
    @Operation(summary = "Get user activity report", 
               description = "Admin views user registration and activity trends")
    public ResponseEntity<ApiResponse<SystemReportDTO>> getUserActivityReport(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Admin generating user activity report from {} to {}", startDate, endDate);
        
        SystemReportDTO report = adminService.generateSystemReport("USER_ACTIVITY", startDate, endDate);
        
        return ResponseEntity.ok(
                ApiResponse.success("User activity report generated successfully", report));
    }

    @GetMapping("/reports/revenue")
    @Operation(summary = "Get revenue report", 
               description = "Admin views financial performance and revenue analytics")
    public ResponseEntity<ApiResponse<SystemReportDTO>> getRevenueReport(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Admin generating revenue report from {} to {}", startDate, endDate);
        
        SystemReportDTO report = adminService.generateSystemReport("REVENUE", startDate, endDate);
        
        return ResponseEntity.ok(
                ApiResponse.success("Revenue report generated successfully", report));
    }

    // ==================== SYSTEM HEALTH & MONITORING ====================

    @GetMapping("/system/health")
    @Operation(summary = "Get system health status", 
               description = "Admin checks current system health and performance metrics")
    public ResponseEntity<ApiResponse<SystemOverviewDTO.SystemHealth>> getSystemHealth() {
        log.info("Admin checking system health");
        
        SystemOverviewDTO overview = adminService.getSystemOverview();
        
        return ResponseEntity.ok(
                ApiResponse.success("System health retrieved successfully", overview.getSystemHealth()));
    }

    @GetMapping("/system/stats")
    @Operation(summary = "Get quick system statistics", 
               description = "Admin views key system metrics for monitoring")
    public ResponseEntity<ApiResponse<SystemOverviewDTO>> getSystemStats() {
        log.info("Admin requesting quick system statistics");
        
        SystemOverviewDTO stats = adminService.getSystemOverview();
        
        return ResponseEntity.ok(
                ApiResponse.success("System statistics retrieved successfully", stats));
    }

    // ==================== ADVANCED OPERATIONS ====================

    @PostMapping("/maintenance/backup")
    @Operation(summary = "Initiate system backup", 
               description = "Admin initiates a manual system backup")
    public ResponseEntity<ApiResponse<String>> initiateBackup() {
        log.info("Admin initiating system backup");
        
        // Mock implementation - in real scenario would trigger actual backup
        String backupId = "backup_" + System.currentTimeMillis();
        log.info("System backup initiated with ID: {}", backupId);
        
        return ResponseEntity.ok(
                ApiResponse.success("System backup initiated successfully", backupId));
    }

    @PostMapping("/maintenance/cleanup")
    @Operation(summary = "Perform system cleanup", 
               description = "Admin performs system cleanup and optimization")
    public ResponseEntity<ApiResponse<String>> performSystemCleanup() {
        log.info("Admin performing system cleanup");
        
        // Mock implementation - in real scenario would perform actual cleanup
        String cleanupResult = "Cleaned up temporary files, optimized database, cleared cache";
        log.info("System cleanup completed");
        
        return ResponseEntity.ok(
                ApiResponse.success("System cleanup completed successfully", cleanupResult));
    }

    @GetMapping("/analytics/trends")
    @Operation(summary = "Get business analytics trends", 
               description = "Admin views comprehensive business analytics and trends")
    public ResponseEntity<ApiResponse<SystemReportDTO>> getAnalyticsTrends(
            @Parameter(description = "Number of days") @RequestParam(defaultValue = "30") int days) {
        log.info("Admin requesting analytics trends for {} days", days);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        
        SystemReportDTO analytics = adminService.generateSystemReport("ANALYTICS", startDate, endDate);
        
        return ResponseEntity.ok(
                ApiResponse.success("Analytics trends retrieved successfully", analytics));
    }

    @GetMapping("/performance/metrics")
    @Operation(summary = "Get performance metrics", 
               description = "Admin views system performance and efficiency metrics")
    public ResponseEntity<ApiResponse<SystemOverviewDTO>> getPerformanceMetrics() {
        log.info("Admin requesting performance metrics");
        
        SystemOverviewDTO metrics = adminService.getSystemOverview();
        
        return ResponseEntity.ok(
                ApiResponse.success("Performance metrics retrieved successfully", metrics));
    }
} 