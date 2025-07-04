package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.dto.technician.*;
import com.fix4home.fix4home.service.TechnicianService;
import com.fix4home.fix4home.security.SecurityConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/v1/technicians")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Technician Management", description = "APIs for technician profile and skills management")
@SecurityRequirement(name = "bearerAuth")
public class TechnicianController {

    private final TechnicianService technicianService;

    // ==================== PUBLIC TECHNICIAN ENDPOINTS ====================

    @GetMapping("/active")
    @Operation(summary = "Get all active technicians", description = "Retrieve list of all approved technicians (public endpoint)")
    public ResponseEntity<ApiResponse<List<TechnicianProfileDTO>>> getActiveTechnicians() {
        log.info("Request to get all active technicians");
        List<TechnicianProfileDTO> technicians = technicianService.getActiveTechnicians();
        return ResponseEntity.ok(ApiResponse.success("Active technicians retrieved successfully", technicians));
    }

    @GetMapping("/search")
    @Operation(summary = "Search technicians", description = "Search technicians by name (public endpoint)")
    public ResponseEntity<ApiResponse<List<TechnicianProfileDTO>>> searchTechnicians(
            @Parameter(description = "Search keyword for technician name")
            @RequestParam(required = false) String keyword) {
        log.info("Request to search technicians with keyword: {}", keyword);
        List<TechnicianProfileDTO> technicians = technicianService.searchTechnicians(keyword);
        return ResponseEntity.ok(ApiResponse.success("Technicians search completed", technicians));
    }

    @GetMapping("/by-rating")
    @Operation(summary = "Get technicians by minimum rating", description = "Retrieve technicians with rating >= specified value (public endpoint)")
    public ResponseEntity<ApiResponse<List<TechnicianProfileDTO>>> getTechniciansByRating(
            @Parameter(description = "Minimum rating filter (0-5)")
            @RequestParam Float minRating) {
        log.info("Request to get technicians with rating >= {}", minRating);
        List<TechnicianProfileDTO> technicians = technicianService.getTechniciansByRating(minRating);
        return ResponseEntity.ok(ApiResponse.success("Technicians filtered by rating successfully", technicians));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get technician profile", description = "Get specific technician profile by user ID (public endpoint)")
    public ResponseEntity<ApiResponse<TechnicianProfileDTO>> getTechnicianProfile(
            @Parameter(description = "User ID of the technician")
            @PathVariable Long userId) {
        log.info("Request to get technician profile for user ID: {}", userId);
        TechnicianProfileDTO technician = technicianService.getTechnicianProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Technician profile retrieved successfully", technician));
    }

    @GetMapping("/{userId}/skills")
    @Operation(summary = "Get technician skills", description = "Get skills of a specific technician (public endpoint)")
    public ResponseEntity<ApiResponse<List<SkillDTO>>> getTechnicianSkills(
            @Parameter(description = "User ID of the technician")
            @PathVariable Long userId) {
        log.info("Request to get skills for technician user ID: {}", userId);
        List<SkillDTO> skills = technicianService.getTechnicianSkills(userId);
        return ResponseEntity.ok(ApiResponse.success("Technician skills retrieved successfully", skills));
    }

    // ==================== TECHNICIAN SELF-MANAGEMENT ENDPOINTS ====================

    @GetMapping("/me")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Get my profile", description = "Get current technician's profile")
    public ResponseEntity<ApiResponse<TechnicianProfileDTO>> getMyProfile() {
        log.info("Request to get current technician's profile");
        TechnicianProfileDTO profile = technicianService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.success("Your profile retrieved successfully", profile));
    }

    @PutMapping("/me")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Update my profile", description = "Update current technician's profile")
    public ResponseEntity<ApiResponse<TechnicianProfileDTO>> updateMyProfile(
            @Valid @RequestBody UpdateTechnicianProfileRequest request) {
        log.info("Request to update current technician's profile");
        TechnicianProfileDTO updatedProfile = technicianService.updateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Your profile updated successfully", updatedProfile));
    }

    @GetMapping("/me/skills")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Get my skills", description = "Get current technician's skills")
    public ResponseEntity<ApiResponse<List<SkillDTO>>> getMySkills() {
        log.info("Request to get current technician's skills");
        List<SkillDTO> skills = technicianService.getMySkills();
        return ResponseEntity.ok(ApiResponse.success("Your skills retrieved successfully", skills));
    }

    @PutMapping("/me/skills")
    @PreAuthorize(SecurityConstants.HAS_TECHNICIAN_ROLE)
    @Operation(summary = "Update my skills", description = "Assign skills to current technician")
    public ResponseEntity<ApiResponse<List<SkillDTO>>> updateMySkills(
            @Valid @RequestBody AssignSkillsRequest request) {
        log.info("Request to update current technician's skills");
        List<SkillDTO> skills = technicianService.assignSkillsToMyself(request);
        return ResponseEntity.ok(ApiResponse.success("Your skills updated successfully", skills));
    }

    // ==================== ADMIN TECHNICIAN MANAGEMENT ENDPOINTS ====================

    @GetMapping
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get all technicians", description = "Get all technicians (admin only)")
    public ResponseEntity<ApiResponse<List<TechnicianProfileDTO>>> getAllTechnicians() {
        log.info("Admin request to get all technicians");
        List<TechnicianProfileDTO> technicians = technicianService.getAllTechnicians();
        return ResponseEntity.ok(ApiResponse.success("All technicians retrieved successfully", technicians));
    }

    @GetMapping("/paginated")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get technicians with pagination", description = "Get technicians with pagination (admin only)")
    public ResponseEntity<ApiResponse<Page<TechnicianProfileDTO>>> getTechniciansWithPagination(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("Admin request to get technicians with pagination - page: {}, size: {}", page, size);
        Page<TechnicianProfileDTO> technicians = technicianService.getAllTechniciansWithPagination(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Technicians retrieved with pagination", technicians));
    }

    @GetMapping("/pending")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get pending technicians", description = "Get technicians pending approval (admin only)")
    public ResponseEntity<ApiResponse<List<TechnicianProfileDTO>>> getPendingTechnicians() {
        log.info("Admin request to get pending technicians");
        List<TechnicianProfileDTO> technicians = technicianService.getPendingTechnicians();
        return ResponseEntity.ok(ApiResponse.success("Pending technicians retrieved successfully", technicians));
    }

    @PutMapping("/{userId}")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Update technician profile", description = "Update technician profile (admin only)")
    public ResponseEntity<ApiResponse<TechnicianProfileDTO>> updateTechnicianProfile(
            @Parameter(description = "User ID of the technician")
            @PathVariable Long userId,
            @Valid @RequestBody UpdateTechnicianProfileRequest request) {
        log.info("Admin request to update technician profile for user ID: {}", userId);
        TechnicianProfileDTO updatedProfile = technicianService.updateTechnicianProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Technician profile updated successfully", updatedProfile));
    }

    @PutMapping("/{userId}/skills")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Assign skills to technician", description = "Assign skills to technician (admin only)")
    public ResponseEntity<ApiResponse<List<SkillDTO>>> assignSkillsToTechnician(
            @Parameter(description = "User ID of the technician")
            @PathVariable Long userId,
            @Valid @RequestBody AssignSkillsRequest request) {
        log.info("Admin request to assign skills to technician user ID: {}", userId);
        List<SkillDTO> skills = technicianService.assignSkillsToTechnician(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Skills assigned to technician successfully", skills));
    }

    // ==================== TECHNICIAN APPROVAL WORKFLOW ENDPOINTS ====================

    @PutMapping("/{userId}/approve")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Approve technician", description = "Approve technician registration (admin only)")
    public ResponseEntity<ApiResponse<TechnicianProfileDTO>> approveTechnician(
            @Parameter(description = "User ID of the technician to approve")
            @PathVariable Long userId) {
        log.info("Admin request to approve technician with user ID: {}", userId);
        TechnicianProfileDTO approvedTechnician = technicianService.approveTechnician(userId);
        return ResponseEntity.ok(ApiResponse.success("Technician approved successfully", approvedTechnician));
    }

    @PutMapping("/{userId}/reject")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Reject technician", description = "Reject technician registration (admin only)")
    public ResponseEntity<ApiResponse<TechnicianProfileDTO>> rejectTechnician(
            @Parameter(description = "User ID of the technician to reject")
            @PathVariable Long userId) {
        log.info("Admin request to reject technician with user ID: {}", userId);
        TechnicianProfileDTO rejectedTechnician = technicianService.rejectTechnician(userId);
        return ResponseEntity.ok(ApiResponse.success("Technician rejected successfully", rejectedTechnician));
    }

    // ==================== SKILLS MANAGEMENT ENDPOINTS ====================

    @GetMapping("/skills")
    @Operation(summary = "Get all skills", description = "Get all available skills (public endpoint)")
    public ResponseEntity<ApiResponse<List<SkillDTO>>> getAllSkills() {
        log.info("Request to get all skills");
        List<SkillDTO> skills = technicianService.getAllSkills();
        return ResponseEntity.ok(ApiResponse.success("All skills retrieved successfully", skills));
    }

    @GetMapping("/skills/search")
    @Operation(summary = "Search skills", description = "Search skills by name (public endpoint)")
    public ResponseEntity<ApiResponse<List<SkillDTO>>> searchSkills(
            @Parameter(description = "Search keyword for skill name")
            @RequestParam(required = false) String keyword) {
        log.info("Request to search skills with keyword: {}", keyword);
        List<SkillDTO> skills = technicianService.searchSkills(keyword);
        return ResponseEntity.ok(ApiResponse.success("Skills search completed", skills));
    }

    @PostMapping("/skills")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Create new skill", description = "Create a new skill (admin only)")
    public ResponseEntity<ApiResponse<SkillDTO>> createSkill(
            @Valid @RequestBody CreateSkillRequest request) {
        log.info("Admin request to create new skill: {}", request.getName());
        SkillDTO createdSkill = technicianService.createSkill(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Skill created successfully", createdSkill));
    }

    @DeleteMapping("/skills/{skillId}")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Delete skill", description = "Delete a skill (admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(
            @Parameter(description = "ID of the skill to delete")
            @PathVariable Long skillId) {
        log.info("Admin request to delete skill with ID: {}", skillId);
        technicianService.deleteSkill(skillId);
        return ResponseEntity.ok(ApiResponse.success("Skill deleted successfully", null));
    }
} 