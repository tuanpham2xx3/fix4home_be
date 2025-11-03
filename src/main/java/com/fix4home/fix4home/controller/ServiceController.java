package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.dto.service.CreateServiceRequest;
import com.fix4home.fix4home.model.dto.service.ServiceDTO;
import com.fix4home.fix4home.model.dto.service.UpdateServiceRequest;
import com.fix4home.fix4home.service.ServiceService;
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
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Service Management", description = "APIs for managing services")
public class ServiceController {

    private final ServiceService serviceService;

    @GetMapping
    @Operation(summary = "Get all services", description = "Retrieve all services (for admin) or active services (for others)")
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getAllServices() {
        log.info("GET /api/v1/services - Fetching all services");
        List<ServiceDTO> services = serviceService.getActiveServices();
        return ResponseEntity.ok(ApiResponse.success("Services retrieved successfully", services));
    }

    @GetMapping("/admin/all")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get all services (Admin only)", description = "Retrieve all services including inactive ones - Admin only")
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getAllServicesForAdmin() {
        log.info("GET /api/v1/services/admin/all - Admin fetching all services");
        List<ServiceDTO> services = serviceService.getAllServices();
        return ResponseEntity.ok(ApiResponse.success("All services retrieved successfully", services));
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get services with pagination", description = "Retrieve services with pagination support")
    public ResponseEntity<ApiResponse<Page<ServiceDTO>>> getServicesWithPagination(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("GET /api/v1/services/paginated - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                 page, size, sortBy, sortDir);
        
        Page<ServiceDTO> services = serviceService.getAllServicesWithPagination(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Services retrieved successfully", services));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service by ID", description = "Retrieve a specific service by its ID")
    public ResponseEntity<ApiResponse<ServiceDTO>> getServiceById(
            @Parameter(description = "Service ID") @PathVariable Long id) {
        log.info("GET /api/v1/services/{} - Fetching service by ID", id);
        ServiceDTO service = serviceService.getServiceById(id);
        return ResponseEntity.ok(ApiResponse.success("Service retrieved successfully", service));
    }

    @GetMapping("/search")
    @Operation(summary = "Search services", description = "Search services by name keyword")
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> searchServices(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        log.info("GET /api/v1/services/search - keyword: {}", keyword);
        List<ServiceDTO> services = serviceService.searchServices(keyword);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", services));
    }

    @PostMapping
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Create new service", description = "Create a new service - Admin only")
    public ResponseEntity<ApiResponse<ServiceDTO>> createService(
            @Valid @RequestBody CreateServiceRequest request) {
        log.info("POST /api/v1/services - Creating new service: {}", request.getName());
        ServiceDTO createdService = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service created successfully", createdService));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Update service", description = "Update an existing service - Admin only")
    public ResponseEntity<ApiResponse<ServiceDTO>> updateService(
            @Parameter(description = "Service ID") @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequest request) {
        log.info("PUT /api/v1/services/{} - Updating service", id);
        ServiceDTO updatedService = serviceService.updateService(id, request);
        return ResponseEntity.ok(ApiResponse.success("Service updated successfully", updatedService));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Soft delete service", description = "Soft delete a service (set status to INACTIVE) - Admin only")
    public ResponseEntity<ApiResponse<String>> deleteService(
            @Parameter(description = "Service ID") @PathVariable Long id) {
        log.info("DELETE /api/v1/services/{} - Soft deleting service", id);
        serviceService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.success("Service deleted successfully", null));
    }

    @DeleteMapping("/{id}/hard")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Hard delete service", description = "Permanently delete a service from database - Admin only")
    public ResponseEntity<ApiResponse<String>> hardDeleteService(
            @Parameter(description = "Service ID") @PathVariable Long id) {
        log.info("DELETE /api/v1/services/{}/hard - Hard deleting service", id);
        serviceService.hardDeleteService(id);
        return ResponseEntity.ok(ApiResponse.success("Service permanently deleted", null));
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Toggle service status", description = "Toggle service status between ACTIVE and INACTIVE - Admin only")
    public ResponseEntity<ApiResponse<ServiceDTO>> toggleServiceStatus(
            @Parameter(description = "Service ID") @PathVariable Long id) {
        log.info("PATCH /api/v1/services/{}/toggle-status - Toggling service status", id);
        ServiceDTO updatedService = serviceService.toggleServiceStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Service status toggled successfully", updatedService));
    }

    // Public endpoints for customers to browse services
    @GetMapping("/active")
    @Operation(summary = "Get active services", description = "Retrieve only active services for public browsing")
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getActiveServices() {
        log.info("GET /api/v1/services/active - Fetching active services");
        List<ServiceDTO> services = serviceService.getActiveServices();
        return ResponseEntity.ok(ApiResponse.success("Active services retrieved successfully", services));
    }

    // Health check endpoint for this controller
    @GetMapping("/health")
    @Operation(summary = "Service controller health check", description = "Health check for service management endpoints")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Service Controller is working", "OK"));
    }
} 