package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.exception.BadRequestException;
import com.fix4home.fix4home.exception.ResourceNotFoundException;
import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.dto.file.FileMetadataDTO;
import com.fix4home.fix4home.model.dto.file.FileUploadRequest;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.FileType;
import com.fix4home.fix4home.security.SecurityConstants;
import com.fix4home.fix4home.security.SecurityHelper;
import com.fix4home.fix4home.service.FileManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Image Management", description = "APIs for managing images")
public class ImageController {
    
    private final FileManagementService fileManagementService;
    
    // ==================== IMAGE UPLOAD ====================
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Upload image", description = "Upload a single image file (Admin only)")
    public ResponseEntity<ApiResponse<FileMetadataDTO>> uploadImage(
            @Parameter(description = "Image file to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Image description") @RequestParam(required = false) String description,
            @Parameter(description = "Make image public (true/false)") @RequestParam(required = false) String isPublicStr) {
        
        log.info("POST /api/v1/images/upload - Uploading image: {}", file.getOriginalFilename());
        log.debug("Upload details - filename: {}, size: {}, contentType: {}, description: {}, isPublic: {}", 
                file.getOriginalFilename(), file.getSize(), file.getContentType(), description, isPublicStr);
        
        try {
            // Validate image file
            validateImageFile(file);
            
            // Get current user - validate it's not null
            User currentUser = SecurityHelper.getCurrentUser();
            if (currentUser == null) {
                log.warn("Upload attempt without authenticated user");
                throw new BadRequestException("User not authenticated. Please login and try again.");
            }
            
            log.debug("Current user: {}", currentUser.getUsername());
            
            // Convert string to Boolean (handle "true", "false", null)
            Boolean isPublic = parseBoolean(isPublicStr, true);
            log.debug("Parsed isPublic: {}", isPublic);
            
            FileUploadRequest request = FileUploadRequest.builder()
                    .entityType("IMAGE")
                    .description(description)
                    .isPublic(isPublic)
                    .build();
            
            FileMetadataDTO image = fileManagementService.uploadSingleFile(
                    file, request, currentUser);
            
            // Verify it's an image
            if (image.getFileType() != FileType.IMAGE) {
                log.warn("Uploaded file is not an image type: {}", image.getFileType());
                throw new BadRequestException("Uploaded file is not an image");
            }
            
            log.info("Image uploaded successfully: ID={}, URL={}", image.getId(), image.getFileUrl());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Image uploaded successfully", image));
                    
        } catch (BadRequestException e) {
            log.error("Bad request during image upload: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during image upload: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to upload image: " + e.getMessage());
        }
    }
    
    // ==================== IMAGE LIST ====================
    
    @GetMapping
    @Operation(summary = "Get images", description = "Get paginated list of images with optional filters")
    public ResponseEntity<ApiResponse<Page<FileMetadataDTO>>> getImages(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by public images only") @RequestParam(required = false) Boolean isPublic) {
        
        log.info("GET /api/v1/images - Getting images, page: {}, size: {}, isPublic: {}", page, size, isPublic);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FileMetadataDTO> images = fileManagementService.getImagesByType(isPublic, pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success("Images retrieved successfully", images));
    }
    
    // ==================== IMAGE DETAIL ====================
    
    @GetMapping("/{id}")
    @Operation(summary = "Get image details", description = "Get detailed information about a specific image")
    public ResponseEntity<ApiResponse<FileMetadataDTO>> getImage(
            @Parameter(description = "Image ID") @PathVariable Long id) {
        
        log.info("GET /api/v1/images/{} - Getting image details", id);
        
        FileMetadataDTO image = fileManagementService.getFileMetadata(id);
        
        // Verify it's an image
        if (image.getFileType() != FileType.IMAGE) {
            throw new ResourceNotFoundException("Image not found with ID: " + id);
        }
        
        return ResponseEntity.ok(
                ApiResponse.success("Image retrieved successfully", image));
    }
    
    // ==================== IMAGE UPDATE ====================
    
    @PutMapping("/{id}")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Update image metadata", description = "Update description and visibility of an image")
    public ResponseEntity<ApiResponse<FileMetadataDTO>> updateImage(
            @Parameter(description = "Image ID") @PathVariable Long id,
            @Parameter(description = "Update data") @RequestBody(required = false) java.util.Map<String, Object> updateData) {
        
        log.info("PUT /api/v1/images/{} - Updating image metadata with data: {}", id, updateData);
        
        // Verify it's an image before updating
        FileMetadataDTO existingImage = fileManagementService.getFileMetadata(id);
        if (existingImage.getFileType() != FileType.IMAGE) {
            throw new ResourceNotFoundException("Image not found with ID: " + id);
        }
        
        // Extract description and isPublic from request body
        String description = null;
        Boolean isPublic = null;
        
        if (updateData != null) {
            if (updateData.containsKey("description")) {
                Object descObj = updateData.get("description");
                description = descObj != null ? descObj.toString() : null;
            }
            
            if (updateData.containsKey("isPublic")) {
                Object isPublicObj = updateData.get("isPublic");
                if (isPublicObj != null) {
                    if (isPublicObj instanceof Boolean) {
                        isPublic = (Boolean) isPublicObj;
                    } else if (isPublicObj instanceof String) {
                        isPublic = parseBoolean((String) isPublicObj, false);
                    } else {
                        isPublic = Boolean.parseBoolean(isPublicObj.toString());
                    }
                }
            }
        }
        
        log.debug("Extracted update data - description: {}, isPublic: {}", description, isPublic);
        
        FileMetadataDTO updatedImage = fileManagementService.updateFileMetadata(id, description, isPublic);
        
        return ResponseEntity.ok(
                ApiResponse.success("Image updated successfully", updatedImage));
    }
    
    // ==================== IMAGE DELETE ====================
    
    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Delete image", description = "Delete an image and its metadata")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @Parameter(description = "Image ID") @PathVariable Long id) {
        
        log.info("DELETE /api/v1/images/{} - Deleting image", id);
        
        // Verify it's an image before deleting
        FileMetadataDTO existingImage = fileManagementService.getFileMetadata(id);
        if (existingImage.getFileType() != FileType.IMAGE) {
            throw new ResourceNotFoundException("Image not found with ID: " + id);
        }
        
        fileManagementService.deleteFile(id);
        
        return ResponseEntity.ok(
                ApiResponse.success("Image deleted successfully", null));
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * Validate that the uploaded file is an image
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Image file cannot be empty");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("File must be an image. Content type: " + contentType);
        }
        
        // Validate file extension
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new BadRequestException("Image filename cannot be empty");
        }
        
        String extension = getFileExtension(filename);
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "bmp");
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new BadRequestException("Unsupported image format: " + extension + 
                    ". Allowed formats: " + String.join(", ", allowedExtensions));
        }
        
        // Validate file size (max 10MB for images)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new BadRequestException("Image size must not exceed 10MB. Current size: " + 
                    (file.getSize() / 1024 / 1024) + "MB");
        }
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1);
    }
    
    /**
     * Parse string to Boolean, handling null and various string formats
     * Supports: "true", "false", "1", "0", "yes", "no" (case insensitive)
     * 
     * @param value String value to parse
     * @param defaultValue Default value if value is null or empty
     * @return Boolean value
     */
    private Boolean parseBoolean(String value, Boolean defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        String lowerValue = value.toLowerCase().trim();
        
        // Handle true values
        if ("true".equals(lowerValue) || "1".equals(lowerValue) || "yes".equals(lowerValue)) {
            return true;
        }
        
        // Handle false values
        if ("false".equals(lowerValue) || "0".equals(lowerValue) || "no".equals(lowerValue)) {
            return false;
        }
        
        // If unrecognized format, return default
        log.warn("Unrecognized boolean value: '{}', using default: {}", value, defaultValue);
        return defaultValue;
    }
}

