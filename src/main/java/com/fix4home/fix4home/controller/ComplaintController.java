package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.dto.complaint.*;
import com.fix4home.fix4home.model.enums.ComplaintStatus;
import com.fix4home.fix4home.service.ComplaintService;
import com.fix4home.fix4home.security.SecurityConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/complaints")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Complaint Controller", description = "Complaint management APIs for handling disputes and issues")
public class ComplaintController {

    private final ComplaintService complaintService;

    // ==================== CUSTOMER & TECHNICIAN OPERATIONS ====================

    @PostMapping
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Create new complaint", 
               description = "Customer or technician files a complaint against the other party in a service request")
    public ResponseEntity<ApiResponse<ComplaintDTO>> createComplaint(
            @Valid @RequestBody CreateComplaintRequest request) {
        log.info("Creating complaint for service request ID: {} against user ID: {}", 
                 request.getServiceRequestId(), request.getAccusedId());
        
        ComplaintDTO complaint = complaintService.createComplaint(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint filed successfully", complaint));
    }

    @GetMapping("/my")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Get my complaints", 
               description = "View all complaints filed by the current user")
    public ResponseEntity<ApiResponse<List<ComplaintDTO>>> getMyComplaints() {
        log.info("Fetching complaints for current user");
        
        List<ComplaintDTO> complaints = complaintService.getMyComplaints();
        
        return ResponseEntity.ok(
                ApiResponse.success("Your complaints retrieved successfully", complaints));
    }

    @GetMapping("/against-me")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Get complaints against me", 
               description = "View all complaints filed against the current user")
    public ResponseEntity<ApiResponse<List<ComplaintDTO>>> getComplaintsAgainstMe() {
        log.info("Fetching complaints against current user");
        
        List<ComplaintDTO> complaints = complaintService.getComplaintsAgainstMe();
        
        return ResponseEntity.ok(
                ApiResponse.success("Complaints against you retrieved successfully", complaints));
    }

    @GetMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get complaint details", 
               description = "View detailed information about a specific complaint")
    public ResponseEntity<ApiResponse<ComplaintDTO>> getComplaintById(
            @Parameter(description = "Complaint ID") @PathVariable Long id) {
        log.info("Fetching complaint details for ID: {}", id);
        
        ComplaintDTO complaint = complaintService.getComplaintById(id);
        
        return ResponseEntity.ok(
                ApiResponse.success("Complaint details retrieved successfully", complaint));
    }

    // ==================== ADMIN OPERATIONS ====================

    @GetMapping
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get all complaints (Admin)", 
               description = "Admin views all complaints with pagination and sorting")
    public ResponseEntity<ApiResponse<Page<ComplaintDTO>>> getAllComplaints(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("Admin fetching all complaints - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                 page, size, sortBy, sortDir);
        
        Page<ComplaintDTO> complaints = complaintService.getAllComplaints(page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(
                ApiResponse.success("All complaints retrieved successfully", complaints));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get complaints by status (Admin)", 
               description = "Admin views complaints filtered by specific status")
    public ResponseEntity<ApiResponse<List<ComplaintDTO>>> getComplaintsByStatus(
            @Parameter(description = "Complaint status") @PathVariable ComplaintStatus status) {
        log.info("Admin fetching complaints by status: {}", status);
        
        List<ComplaintDTO> complaints = complaintService.getComplaintsByStatus(status);
        
        return ResponseEntity.ok(
                ApiResponse.success("Complaints by status retrieved successfully", complaints));
    }

    @GetMapping("/pending")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get pending complaints (Admin)", 
               description = "Admin views all complaints awaiting review, ordered by creation date")
    public ResponseEntity<ApiResponse<List<ComplaintDTO>>> getPendingComplaints() {
        log.info("Admin fetching pending complaints");
        
        List<ComplaintDTO> complaints = complaintService.getPendingComplaints();
        
        return ResponseEntity.ok(
                ApiResponse.success("Pending complaints retrieved successfully", complaints));
    }

    @PutMapping("/{id}/investigate")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Start complaint investigation (Admin)", 
               description = "Admin starts investigating a pending complaint")
    public ResponseEntity<ApiResponse<ComplaintDTO>> startInvestigation(
            @Parameter(description = "Complaint ID") @PathVariable Long id) {
        log.info("Admin starting investigation for complaint ID: {}", id);
        
        ComplaintDTO complaint = complaintService.startInvestigation(id);
        
        return ResponseEntity.ok(
                ApiResponse.success("Complaint investigation started successfully", complaint));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Resolve complaint (Admin)", 
               description = "Admin resolves or rejects a complaint with admin response")
    public ResponseEntity<ApiResponse<ComplaintDTO>> resolveComplaint(
            @Parameter(description = "Complaint ID") @PathVariable Long id,
            @Valid @RequestBody ResolveComplaintRequest request) {
        log.info("Admin resolving complaint ID: {} with status: {}", id, request.getStatus());
        
        ComplaintDTO complaint = complaintService.resolveComplaint(id, request);
        
        return ResponseEntity.ok(
                ApiResponse.success("Complaint resolved successfully", complaint));
    }

    @GetMapping("/stats")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get complaint statistics (Admin)", 
               description = "Admin views comprehensive statistics about complaints and resolution metrics")
    public ResponseEntity<ApiResponse<ComplaintStatsDTO>> getComplaintStats() {
        log.info("Admin generating complaint statistics");
        
        ComplaintStatsDTO stats = complaintService.getComplaintStats();
        
        return ResponseEntity.ok(
                ApiResponse.success("Complaint statistics retrieved successfully", stats));
    }

    // ==================== QUICK ACCESS ENDPOINTS ====================

    @GetMapping("/urgent")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get urgent complaints (Admin)", 
               description = "Admin views complaints that have been pending for more than 48 hours")
    public ResponseEntity<ApiResponse<List<ComplaintDTO>>> getUrgentComplaints() {
        log.info("Admin fetching urgent complaints");
        
        List<ComplaintDTO> urgentComplaints = complaintService.getComplaintsByStatus(ComplaintStatus.PENDING)
                .stream()
                .filter(complaint -> {
                    if (complaint.getCreatedAt() != null) {
                        return complaint.getCreatedAt().isBefore(java.time.LocalDateTime.now().minusHours(48));
                    }
                    return false;
                })
                .toList();
        
        return ResponseEntity.ok(
                ApiResponse.success("Urgent complaints retrieved successfully", urgentComplaints));
    }

    @GetMapping("/health")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Complaint system health check (Admin)", 
               description = "Quick health check for complaint management system")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        log.info("Complaint system health check requested");
        
        ComplaintStatsDTO stats = complaintService.getComplaintStats();
        String healthStatus = "System operational. Total complaints: " + stats.getTotalComplaints() + 
                             ", Pending: " + stats.getPendingComplaints() + 
                             ", Urgent: " + stats.getUrgentComplaintsCount();
        
        return ResponseEntity.ok(
                ApiResponse.success("Complaint system is healthy", healthStatus));
    }
} 