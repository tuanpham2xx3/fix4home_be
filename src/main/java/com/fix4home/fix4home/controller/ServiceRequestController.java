package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.dto.servicerequest.*;
import com.fix4home.fix4home.model.enums.ServiceRequestStatus;
import com.fix4home.fix4home.service.ServiceRequestService;
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
@RequestMapping("/api/v1/service-requests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Service Request Controller", description = "Core booking workflow APIs for managing service requests")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    // ==================== CUSTOMER OPERATIONS ====================

    @PostMapping
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Create new service request", 
               description = "Customer creates a new service request for a specific service and address")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> createServiceRequest(
            @Valid @RequestBody CreateServiceRequestRequest request) {
        log.info("Creating service request for service ID: {}", request.getServiceId());
        
        ServiceRequestDTO serviceRequest = serviceRequestService.createServiceRequest(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service request created successfully", serviceRequest));
    }

    @GetMapping("/my")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Get my service requests", 
               description = "Customer views all their service requests")
    public ResponseEntity<ApiResponse<List<ServiceRequestDTO>>> getMyServiceRequests() {
        log.info("Fetching service requests for current customer");
        
        List<ServiceRequestDTO> serviceRequests = serviceRequestService.getMyServiceRequests();
        
        return ResponseEntity.ok(
                ApiResponse.success("Service requests retrieved successfully", serviceRequests));
    }

    @GetMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get service request details", 
               description = "Get detailed information about a specific service request")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> getServiceRequestById(
            @Parameter(description = "Service request ID") @PathVariable Long id) {
        log.info("Fetching service request details for ID: {}", id);
        
        ServiceRequestDTO serviceRequest = serviceRequestService.getServiceRequestById(id);
        
        return ResponseEntity.ok(
                ApiResponse.success("Service request details retrieved successfully", serviceRequest));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_ADMIN_ROLE)
    @Operation(summary = "Cancel service request", 
               description = "Customer or admin cancels a service request")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> cancelServiceRequest(
            @Parameter(description = "Service request ID") @PathVariable Long id) {
        log.info("Canceling service request with ID: {}", id);
        
        ServiceRequestDTO serviceRequest = serviceRequestService.cancelServiceRequest(id);
        
        return ResponseEntity.ok(
                ApiResponse.success("Service request canceled successfully", serviceRequest));
    }

    // ==================== TECHNICIAN OPERATIONS ====================

    @GetMapping("/available")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Get available service requests", 
               description = "Technician views all unassigned service requests available for acceptance")
    public ResponseEntity<ApiResponse<List<ServiceRequestSummaryDTO>>> getAvailableServiceRequests() {
        log.info("Fetching available service requests for technicians");
        
        List<ServiceRequestSummaryDTO> serviceRequests = serviceRequestService.getAvailableServiceRequests();
        
        return ResponseEntity.ok(
                ApiResponse.success("Available service requests retrieved successfully", serviceRequests));
    }

    @GetMapping("/my-assignments")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Get my assigned requests", 
               description = "Technician views all their assigned service requests")
    public ResponseEntity<ApiResponse<List<ServiceRequestDTO>>> getMyAssignedRequests() {
        log.info("Fetching assigned requests for current technician");
        
        List<ServiceRequestDTO> serviceRequests = serviceRequestService.getMyAssignedRequests();
        
        return ResponseEntity.ok(
                ApiResponse.success("Assigned service requests retrieved successfully", serviceRequests));
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Accept service request", 
               description = "Technician accepts an available service request")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> acceptServiceRequest(
            @Parameter(description = "Service request ID") @PathVariable Long id) {
        log.info("Technician accepting service request with ID: {}", id);
        
        ServiceRequestDTO serviceRequest = serviceRequestService.acceptServiceRequest(id);
        
        return ResponseEntity.ok(
                ApiResponse.success("Service request accepted successfully", serviceRequest));
    }

    @PutMapping("/{id}/decline")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Decline service request", 
               description = "Technician declines an assigned service request")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> declineServiceRequest(
            @Parameter(description = "Service request ID") @PathVariable Long id) {
        log.info("Technician declining service request with ID: {}", id);
        
        ServiceRequestDTO serviceRequest = serviceRequestService.declineServiceRequest(id);
        
        return ResponseEntity.ok(
                ApiResponse.success("Service request declined successfully", serviceRequest));
    }

    @PutMapping("/{id}/start")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Start work on service request", 
               description = "Technician starts working on an assigned service request")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> startWork(
            @Parameter(description = "Service request ID") @PathVariable Long id) {
        log.info("Technician starting work on service request with ID: {}", id);
        
        ServiceRequestDTO serviceRequest = serviceRequestService.startWork(id);
        
        return ResponseEntity.ok(
                ApiResponse.success("Work started on service request successfully", serviceRequest));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Complete work on service request", 
               description = "Technician marks service request as completed")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> completeWork(
            @Parameter(description = "Service request ID") @PathVariable Long id) {
        log.info("Technician completing work on service request with ID: {}", id);
        
        ServiceRequestDTO serviceRequest = serviceRequestService.completeWork(id);
        
        return ResponseEntity.ok(
                ApiResponse.success("Service request completed successfully", serviceRequest));
    }

    // ==================== ADMIN OPERATIONS ====================

    @GetMapping
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get all service requests (Admin)", 
               description = "Admin views all service requests with pagination and sorting")
    public ResponseEntity<ApiResponse<Page<ServiceRequestSummaryDTO>>> getAllServiceRequests(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("Admin fetching all service requests - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                 page, size, sortBy, sortDir);
        
        Page<ServiceRequestSummaryDTO> serviceRequests = serviceRequestService.getAllServiceRequests(page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(
                ApiResponse.success("All service requests retrieved successfully", serviceRequests));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get service requests by status (Admin)", 
               description = "Admin views service requests filtered by status")
    public ResponseEntity<ApiResponse<List<ServiceRequestSummaryDTO>>> getServiceRequestsByStatus(
            @Parameter(description = "Service request status") @PathVariable ServiceRequestStatus status) {
        log.info("Admin fetching service requests by status: {}", status);
        
        List<ServiceRequestSummaryDTO> serviceRequests = serviceRequestService.getServiceRequestsByStatus(status);
        
        return ResponseEntity.ok(
                ApiResponse.success("Service requests by status retrieved successfully", serviceRequests));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Assign technician to service request (Admin)", 
               description = "Admin assigns a specific technician to a service request")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> assignTechnician(
            @Parameter(description = "Service request ID") @PathVariable Long id,
            @Valid @RequestBody AssignTechnicianRequest request) {
        log.info("Admin assigning technician {} to service request {}", request.getTechnicianId(), id);
        
        ServiceRequestDTO serviceRequest = serviceRequestService.assignTechnician(id, request);
        
        return ResponseEntity.ok(
                ApiResponse.success("Technician assigned to service request successfully", serviceRequest));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Update service request status (Admin)", 
               description = "Admin updates the status of a service request")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> updateServiceRequestStatus(
            @Parameter(description = "Service request ID") @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequestStatusRequest request) {
        log.info("Admin updating status of service request {} to {}", id, request.getStatus());
        
        ServiceRequestDTO serviceRequest = serviceRequestService.updateServiceRequestStatus(id, request);
        
        return ResponseEntity.ok(
                ApiResponse.success("Service request status updated successfully", serviceRequest));
    }

    @GetMapping("/stats")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get service request statistics (Admin)", 
               description = "Admin views comprehensive statistics about service requests")
    public ResponseEntity<ApiResponse<ServiceRequestStatsDTO>> getServiceRequestStats() {
        log.info("Admin generating service request statistics");
        
        ServiceRequestStatsDTO stats = serviceRequestService.getServiceRequestStats();
        
        return ResponseEntity.ok(
                ApiResponse.success("Service request statistics retrieved successfully", stats));
    }
} 