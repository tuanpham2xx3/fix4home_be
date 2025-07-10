package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.dto.servicepost.*;
import com.fix4home.fix4home.security.SecurityConstants;
import com.fix4home.fix4home.service.ServicePostService;
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
@RequestMapping("/api/v1/service-posts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Service Posts Management", description = "APIs for managing service posts")
public class ServicePostController {

    private final ServicePostService servicePostService;

    // ==================== CUSTOMER ENDPOINTS ====================

    @PostMapping
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Create service post", description = "Create a new service post - Customer only")
    public ResponseEntity<ApiResponse<ServicePostDTO>> createServicePost(
            @Valid @RequestBody CreateServicePostRequest request) {
        log.info("POST /api/v1/service-posts - Creating service post: {}", request.getTitle());
        ServicePostDTO createdPost = servicePostService.createServicePost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service post created successfully", createdPost));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Publish service post", description = "Publish a draft service post - Customer only")
    public ResponseEntity<ApiResponse<ServicePostDTO>> publishServicePost(
            @Parameter(description = "Service post ID") @PathVariable Long id) {
        log.info("PUT /api/v1/service-posts/{}/publish - Publishing service post", id);
        ServicePostDTO publishedPost = servicePostService.publishServicePost(id);
        return ResponseEntity.ok(ApiResponse.success("Service post published successfully", publishedPost));
    }

    @GetMapping("/my")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Get my service posts", description = "Get all service posts created by current customer")
    public ResponseEntity<ApiResponse<List<ServicePostDTO>>> getMyServicePosts() {
        log.info("GET /api/v1/service-posts/my - Fetching customer's service posts");
        List<ServicePostDTO> posts = servicePostService.getMyServicePosts();
        return ResponseEntity.ok(ApiResponse.success("Service posts retrieved successfully", posts));
    }

    @GetMapping("/my/paginated")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Get my service posts with pagination", description = "Get customer's service posts with pagination")
    public ResponseEntity<ApiResponse<Page<ServicePostDTO>>> getMyServicePostsWithPagination(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("GET /api/v1/service-posts/my/paginated - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                page, size, sortBy, sortDir);
        Page<ServicePostDTO> posts = servicePostService.getMyServicePostsWithPagination(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Service posts retrieved successfully", posts));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Update service post", description = "Update an existing service post - Customer only")
    public ResponseEntity<ApiResponse<ServicePostDTO>> updateServicePost(
            @Parameter(description = "Service post ID") @PathVariable Long id,
            @Valid @RequestBody UpdateServicePostRequest request) {
        log.info("PUT /api/v1/service-posts/{} - Updating service post", id);
        ServicePostDTO updatedPost = servicePostService.updateServicePost(id, request);
        return ResponseEntity.ok(ApiResponse.success("Service post updated successfully", updatedPost));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Cancel service post", description = "Cancel a service post - Customer only")
    public ResponseEntity<ApiResponse<Void>> cancelServicePost(
            @Parameter(description = "Service post ID") @PathVariable Long id) {
        log.info("PUT /api/v1/service-posts/{}/cancel - Cancelling service post", id);
        servicePostService.cancelServicePost(id);
        return ResponseEntity.ok(ApiResponse.success("Service post cancelled successfully", null));
    }

    @PutMapping("/{servicePostId}/select-technician/{responseId}")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_ROLE)
    @Operation(summary = "Select technician", description = "Select a technician response for service post - Customer only")
    public ResponseEntity<ApiResponse<ServicePostDTO>> selectTechnician(
            @Parameter(description = "Service post ID") @PathVariable Long servicePostId,
            @Parameter(description = "Response ID") @PathVariable Long responseId) {
        log.info("PUT /api/v1/service-posts/{}/select-technician/{} - Selecting technician", servicePostId, responseId);
        ServicePostDTO updatedPost = servicePostService.selectTechnician(servicePostId, responseId);
        return ResponseEntity.ok(ApiResponse.success("Technician selected successfully", updatedPost));
    }

    // ==================== TECHNICIAN ENDPOINTS ====================

    @GetMapping("/available")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Get available service posts", description = "Get service posts available for technician responses")
    public ResponseEntity<ApiResponse<List<ServicePostSummaryDTO>>> getAvailableServicePosts() {
        log.info("GET /api/v1/service-posts/available - Fetching available service posts for technician");
        List<ServicePostSummaryDTO> posts = servicePostService.getAvailableServicePosts();
        return ResponseEntity.ok(ApiResponse.success("Available service posts retrieved successfully", posts));
    }

    @GetMapping("/available/paginated")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Get available service posts with pagination", description = "Get available service posts with pagination for technicians")
    public ResponseEntity<ApiResponse<Page<ServicePostSummaryDTO>>> getAvailableServicePostsWithPagination(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("GET /api/v1/service-posts/available/paginated - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                page, size, sortBy, sortDir);
        Page<ServicePostSummaryDTO> posts = servicePostService.getAvailableServicePostsWithPagination(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Available service posts retrieved successfully", posts));
    }

    @PostMapping("/{id}/respond")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Respond to service post", description = "Submit a response to a service post - Technician only")
    public ResponseEntity<ApiResponse<ServicePostResponseDTO>> respondToServicePost(
            @Parameter(description = "Service post ID") @PathVariable Long id,
            @Valid @RequestBody CreateServicePostResponseRequest request) {
        log.info("POST /api/v1/service-posts/{}/respond - Technician responding to service post", id);
        ServicePostResponseDTO response = servicePostService.respondToServicePost(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Response submitted successfully", response));
    }

    @GetMapping("/my-responses")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Get my responses", description = "Get all responses submitted by current technician")
    public ResponseEntity<ApiResponse<List<ServicePostResponseDTO>>> getMyResponses() {
        log.info("GET /api/v1/service-posts/my-responses - Fetching technician's responses");
        List<ServicePostResponseDTO> responses = servicePostService.getMyResponses();
        return ResponseEntity.ok(ApiResponse.success("Responses retrieved successfully", responses));
    }

    // ==================== SHARED ENDPOINTS ====================

    @GetMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get service post by ID", description = "Get detailed information about a service post")
    public ResponseEntity<ApiResponse<ServicePostDTO>> getServicePostById(
            @Parameter(description = "Service post ID") @PathVariable Long id) {
        log.info("GET /api/v1/service-posts/{} - Fetching service post details", id);
        ServicePostDTO post = servicePostService.getServicePostById(id);
        return ResponseEntity.ok(ApiResponse.success("Service post retrieved successfully", post));
    }

    @GetMapping("/{id}/responses")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_ADMIN_ROLE)
    @Operation(summary = "Get service post responses", description = "Get all responses for a service post - Customer or Admin only")
    public ResponseEntity<ApiResponse<List<ServicePostResponseDTO>>> getServicePostResponses(
            @Parameter(description = "Service post ID") @PathVariable Long id) {
        log.info("GET /api/v1/service-posts/{}/responses - Fetching service post responses", id);
        List<ServicePostResponseDTO> responses = servicePostService.getServicePostResponses(id);
        return ResponseEntity.ok(ApiResponse.success("Responses retrieved successfully", responses));
    }

    @PutMapping("/{id}/start")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Mark service post as in progress", description = "Mark service post as started/in progress")
    public ResponseEntity<ApiResponse<ServicePostDTO>> markServicePostInProgress(
            @Parameter(description = "Service post ID") @PathVariable Long id) {
        log.info("PUT /api/v1/service-posts/{}/start - Marking service post as in progress", id);
        ServicePostDTO updatedPost = servicePostService.markServicePostInProgress(id);
        return ResponseEntity.ok(ApiResponse.success("Service post marked as in progress", updatedPost));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize(SecurityConstants.HAS_CUSTOMER_OR_TECHNICIAN_ROLE)
    @Operation(summary = "Complete service post", description = "Mark service post as completed")
    public ResponseEntity<ApiResponse<ServicePostDTO>> completeServicePost(
            @Parameter(description = "Service post ID") @PathVariable Long id) {
        log.info("PUT /api/v1/service-posts/{}/complete - Completing service post", id);
        ServicePostDTO updatedPost = servicePostService.completeServicePost(id);
        return ResponseEntity.ok(ApiResponse.success("Service post completed successfully", updatedPost));
    }

    // ==================== SEARCH ENDPOINTS ====================

    @GetMapping("/search")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Search service posts", description = "Search service posts by keyword")
    public ResponseEntity<ApiResponse<List<ServicePostSummaryDTO>>> searchServicePosts(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        log.info("GET /api/v1/service-posts/search - keyword: {}", keyword);
        List<ServicePostSummaryDTO> posts = servicePostService.searchServicePosts(keyword);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", posts));
    }

    @GetMapping("/by-location")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Get service posts by location", description = "Get service posts by location/area")
    public ResponseEntity<ApiResponse<List<ServicePostSummaryDTO>>> getServicePostsByLocation(
            @Parameter(description = "Location keyword") @RequestParam String location) {
        log.info("GET /api/v1/service-posts/by-location - location: {}", location);
        List<ServicePostSummaryDTO> posts = servicePostService.getServicePostsByLocation(location);
        return ResponseEntity.ok(ApiResponse.success("Posts by location retrieved successfully", posts));
    }

    @GetMapping("/urgent")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Get urgent service posts", description = "Get urgent service posts that need immediate attention")
    public ResponseEntity<ApiResponse<List<ServicePostSummaryDTO>>> getUrgentServicePosts() {
        log.info("GET /api/v1/service-posts/urgent - Fetching urgent service posts");
        List<ServicePostSummaryDTO> posts = servicePostService.getUrgentServicePosts();
        return ResponseEntity.ok(ApiResponse.success("Urgent posts retrieved successfully", posts));
    }

    // ==================== ADMIN ENDPOINTS ====================

    @GetMapping("/admin/all")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get all service posts (Admin)", description = "Get all service posts in system - Admin only")
    public ResponseEntity<ApiResponse<List<ServicePostDTO>>> getAllServicePosts() {
        log.info("GET /api/v1/service-posts/admin/all - Admin fetching all service posts");
        List<ServicePostDTO> posts = servicePostService.getAllServicePosts();
        return ResponseEntity.ok(ApiResponse.success("All service posts retrieved successfully", posts));
    }

    @GetMapping("/admin/all/paginated")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get all service posts with pagination (Admin)", description = "Get all service posts with pagination - Admin only")
    public ResponseEntity<ApiResponse<Page<ServicePostDTO>>> getAllServicePostsWithPagination(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("GET /api/v1/service-posts/admin/all/paginated - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                page, size, sortBy, sortDir);
        Page<ServicePostDTO> posts = servicePostService.getAllServicePostsWithPagination(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("All service posts retrieved successfully", posts));
    }

    @PostMapping("/admin/process-expired")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Process expired posts (Admin)", description = "Process and update status of expired service posts - Admin only")
    public ResponseEntity<ApiResponse<Void>> processExpiredPosts() {
        log.info("POST /api/v1/service-posts/admin/process-expired - Processing expired posts");
        servicePostService.processExpiredPosts();
        return ResponseEntity.ok(ApiResponse.success("Expired posts processed successfully", null));
    }

    // ==================== HEALTH CHECK ====================

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Service posts management health check")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Service Posts Management is healthy", "OK"));
    }
} 