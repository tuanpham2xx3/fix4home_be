package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.dto.consultation.*;
import com.fix4home.fix4home.model.enums.ConsultationStatus;
import com.fix4home.fix4home.security.SecurityConstants;
import com.fix4home.fix4home.service.ConsultationService;
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
@RequestMapping("/api/v1/consultations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Consultation & Quotation Management", description = "APIs for managing consultations and quotations")
public class ConsultationController {

    private final ConsultationService consultationService;

    // ==================== TECHNICIAN ENDPOINTS ====================

    @PostMapping
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Submit consultation", description = "Submit a consultation proposal for a service post - Technician only")
    public ResponseEntity<ApiResponse<ConsultationDTO>> submitConsultation(
            @Valid @RequestBody CreateConsultationRequest request) {
        log.info("POST /api/v1/consultations - Submitting consultation for service post: {}", request.getServicePostId());
        ConsultationDTO consultation = consultationService.submitConsultation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Consultation submitted successfully", consultation));
    }

    @GetMapping("/my-proposals")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Get my consultation proposals", description = "Get all consultation proposals submitted by current technician")
    public ResponseEntity<ApiResponse<List<ConsultationDTO>>> getMyConsultations() {
        log.info("GET /api/v1/consultations/my-proposals - Fetching technician's consultations");
        List<ConsultationDTO> consultations = consultationService.getMyConsultations();
        return ResponseEntity.ok(ApiResponse.success("Consultations retrieved successfully", consultations));
    }

    @GetMapping("/my-proposals/paginated")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Get my consultation proposals with pagination", description = "Get technician's consultations with pagination")
    public ResponseEntity<ApiResponse<Page<ConsultationDTO>>> getMyConsultationsWithPagination(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "submittedAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("GET /api/v1/consultations/my-proposals/paginated - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                page, size, sortBy, sortDir);
        Page<ConsultationDTO> consultations = consultationService.getMyConsultationsWithPagination(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Consultations retrieved successfully", consultations));
    }

    @GetMapping("/my-proposals/by-status")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Get my consultations by status", description = "Get technician's consultations filtered by status")
    public ResponseEntity<ApiResponse<List<ConsultationDTO>>> getMyConsultationsByStatus(
            @Parameter(description = "Consultation status") @RequestParam ConsultationStatus status) {
        log.info("GET /api/v1/consultations/my-proposals/by-status - status: {}", status);
        List<ConsultationDTO> consultations = consultationService.getMyConsultationsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Consultations retrieved successfully", consultations));
    }

    // ==================== CUSTOMER ENDPOINTS ====================

    @GetMapping("/post/{servicePostId}")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Get consultations for service post", description = "Get all consultations for a specific service post - Customer only")
    public ResponseEntity<ApiResponse<List<ConsultationDTO>>> getConsultationsForServicePost(
            @Parameter(description = "Service post ID") @PathVariable Long servicePostId) {
        log.info("GET /api/v1/consultations/post/{} - Fetching consultations for service post", servicePostId);
        List<ConsultationDTO> consultations = consultationService.getConsultationsForServicePost(servicePostId);
        return ResponseEntity.ok(ApiResponse.success("Consultations retrieved successfully", consultations));
    }

    @GetMapping("/post/{servicePostId}/paginated")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Get consultations for service post with pagination", description = "Get consultations for service post with pagination - Customer only")
    public ResponseEntity<ApiResponse<Page<ConsultationDTO>>> getConsultationsForServicePostWithPagination(
            @Parameter(description = "Service post ID") @PathVariable Long servicePostId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "submittedAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("GET /api/v1/consultations/post/{}/paginated - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                servicePostId, page, size, sortBy, sortDir);
        Page<ConsultationDTO> consultations = consultationService.getConsultationsForServicePostWithPagination(
                servicePostId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Consultations retrieved successfully", consultations));
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Accept consultation", description = "Accept a consultation proposal - Customer only")
    public ResponseEntity<ApiResponse<ConsultationDTO>> acceptConsultation(
            @Parameter(description = "Consultation ID") @PathVariable Long id) {
        log.info("PUT /api/v1/consultations/{}/accept - Accepting consultation", id);
        UpdateConsultationStatusRequest request = UpdateConsultationStatusRequest.builder()
                .status(ConsultationStatus.ACCEPTED)
                .build();
        ConsultationDTO consultation = consultationService.updateConsultationStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Consultation accepted successfully", consultation));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Reject consultation", description = "Reject a consultation proposal - Customer only")
    public ResponseEntity<ApiResponse<ConsultationDTO>> rejectConsultation(
            @Parameter(description = "Consultation ID") @PathVariable Long id,
            @RequestParam(required = false) String rejectionReason) {
        log.info("PUT /api/v1/consultations/{}/reject - Rejecting consultation", id);
        UpdateConsultationStatusRequest request = UpdateConsultationStatusRequest.builder()
                .status(ConsultationStatus.REJECTED)
                .rejectionReason(rejectionReason)
                .build();
        ConsultationDTO consultation = consultationService.updateConsultationStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Consultation rejected successfully", consultation));
    }

    @GetMapping("/my-received")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Get my received consultations", description = "Get all consultations received by current customer")
    public ResponseEntity<ApiResponse<List<ConsultationDTO>>> getMyConsultationsAsCustomer() {
        log.info("GET /api/v1/consultations/my-received - Fetching customer's received consultations");
        List<ConsultationDTO> consultations = consultationService.getMyConsultationsAsCustomer();
        return ResponseEntity.ok(ApiResponse.success("Consultations retrieved successfully", consultations));
    }

    @GetMapping("/my-received/paginated")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Get my received consultations with pagination", description = "Get customer's received consultations with pagination")
    public ResponseEntity<ApiResponse<Page<ConsultationDTO>>> getMyConsultationsAsCustomerWithPagination(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "submittedAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("GET /api/v1/consultations/my-received/paginated - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                page, size, sortBy, sortDir);
        Page<ConsultationDTO> consultations = consultationService.getMyConsultationsAsCustomerWithPagination(
                page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Consultations retrieved successfully", consultations));
    }

    // ==================== SHARED ENDPOINTS ====================

    @GetMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get consultation by ID", description = "Get detailed information about a consultation")
    public ResponseEntity<ApiResponse<ConsultationDTO>> getConsultationById(
            @Parameter(description = "Consultation ID") @PathVariable Long id) {
        log.info("GET /api/v1/consultations/{} - Fetching consultation details", id);
        ConsultationDTO consultation = consultationService.getConsultationById(id);
        return ResponseEntity.ok(ApiResponse.success("Consultation retrieved successfully", consultation));
    }

    // ==================== ADMIN ENDPOINTS ====================

    @GetMapping("/admin/all")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get all consultations (Admin)", description = "Get all consultations in system - Admin only")
    public ResponseEntity<ApiResponse<List<ConsultationDTO>>> getAllConsultations() {
        log.info("GET /api/v1/consultations/admin/all - Fetching all consultations");
        List<ConsultationDTO> consultations = consultationService.getAllConsultations();
        return ResponseEntity.ok(ApiResponse.success("Consultations retrieved successfully", consultations));
    }

    @GetMapping("/admin/all/paginated")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get all consultations with pagination (Admin)", description = "Get all consultations with pagination - Admin only")
    public ResponseEntity<ApiResponse<Page<ConsultationDTO>>> getAllConsultationsWithPagination(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "submittedAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("GET /api/v1/consultations/admin/all/paginated - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                page, size, sortBy, sortDir);
        Page<ConsultationDTO> consultations = consultationService.getAllConsultationsWithPagination(
                page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Consultations retrieved successfully", consultations));
    }

    // ==================== UTILITY ENDPOINTS ====================

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Consultation management health check")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        log.info("GET /api/v1/consultations/health - Health check");
        return ResponseEntity.ok(ApiResponse.success("Consultation service is running", "OK"));
    }
} 