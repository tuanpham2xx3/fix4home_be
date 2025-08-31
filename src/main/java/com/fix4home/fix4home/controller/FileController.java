package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.dto.file.FileMetadataDTO;
import com.fix4home.fix4home.model.dto.file.FileUploadRequest;
import com.fix4home.fix4home.model.dto.file.FileUploadResponse;
import com.fix4home.fix4home.service.FileManagementService;
import com.fix4home.fix4home.security.SecurityConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fix4home.fix4home.security.SecurityHelper;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Management", description = "APIs for file upload, download, and management")
public class FileController {
    
    private final FileManagementService fileManagementService;
    
    // ==================== FILE UPLOAD ENDPOINTS ====================
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Upload files", description = "Upload one or multiple files with metadata")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFiles(
            @Parameter(description = "Files to upload") @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "Entity type", example = "SERVICE_REQUEST") @RequestParam(required = false) String entityType,
            @Parameter(description = "Entity ID", example = "123") @RequestParam(required = false) Long entityId,
            @Parameter(description = "File description", example = "Before repair photo") @RequestParam(required = false) String description,
            @Parameter(description = "Make files public", example = "false") @RequestParam(defaultValue = "false") Boolean isPublic) {
        
        log.info("POST /api/v1/files/upload - Uploading {} files", files.length);
        
        FileUploadRequest request = FileUploadRequest.builder()
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .isPublic(isPublic)
                .build();
        
        FileUploadResponse response = fileManagementService.uploadFiles(files, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Files uploaded successfully", response));
    }
    
    @PostMapping(value = "/upload-single", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Upload single file", description = "Upload a single file with metadata")
    public ResponseEntity<ApiResponse<FileMetadataDTO>> uploadSingleFile(
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute FileUploadRequest request) {
        
        log.info("POST /api/v1/files/upload-single - Uploading file: {}", file.getOriginalFilename());
        
        FileMetadataDTO response = fileManagementService.uploadSingleFile(file, request, 
                SecurityHelper.getCurrentUser());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded successfully", response));
    }
    
    // ==================== FILE DOWNLOAD ENDPOINTS ====================
    
    @GetMapping("/download/{filename:.+}")
    @Operation(summary = "Download file", description = "Download a file by its stored filename")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "Stored filename") @PathVariable String filename) {
        
        log.info("GET /api/v1/files/download/{} - Downloading file", filename);
        
        Resource resource = fileManagementService.downloadFile(filename);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    
    @GetMapping("/view/{filename:.+}")
    @Operation(summary = "View file", description = "View a file in browser (for images, PDFs, etc.)")
    public ResponseEntity<Resource> viewFile(
            @Parameter(description = "Stored filename") @PathVariable String filename) {
        
        log.info("GET /api/v1/files/view/{} - Viewing file", filename);
        
        Resource resource = fileManagementService.downloadFile(filename);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    
    // ==================== FILE METADATA ENDPOINTS ====================
    
    @GetMapping("/{fileId}")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Get file metadata", description = "Get metadata for a specific file")
    public ResponseEntity<ApiResponse<FileMetadataDTO>> getFileMetadata(
            @Parameter(description = "File ID") @PathVariable Long fileId) {
        
        log.info("GET /api/v1/files/{} - Getting file metadata", fileId);
        
        FileMetadataDTO fileMetadata = fileManagementService.getFileMetadata(fileId);
        
        return ResponseEntity.ok(ApiResponse.success("File metadata retrieved successfully", fileMetadata));
    }
    
    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Get files by entity", description = "Get all files associated with a specific entity")
    public ResponseEntity<ApiResponse<List<FileMetadataDTO>>> getFilesByEntity(
            @Parameter(description = "Entity type", example = "SERVICE_REQUEST") @PathVariable String entityType,
            @Parameter(description = "Entity ID") @PathVariable Long entityId) {
        
        log.info("GET /api/v1/files/entity/{}/{} - Getting files by entity", entityType, entityId);
        
        List<FileMetadataDTO> files = fileManagementService.getFilesByEntity(entityType, entityId);
        
        return ResponseEntity.ok(ApiResponse.success("Files retrieved successfully", files));
    }
    
    @GetMapping("/my")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Get user's files", description = "Get all files uploaded by current user")
    public ResponseEntity<ApiResponse<Page<FileMetadataDTO>>> getUserFiles(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("GET /api/v1/files/my - Getting user's files");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FileMetadataDTO> files = fileManagementService.getUserFiles(pageable);
        
        return ResponseEntity.ok(ApiResponse.success("User files retrieved successfully", files));
    }
    
    // ==================== FILE MANAGEMENT ENDPOINTS ====================
    
    @PutMapping("/{fileId}")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Update file metadata", description = "Update description and visibility of a file")
    public ResponseEntity<ApiResponse<FileMetadataDTO>> updateFileMetadata(
            @Parameter(description = "File ID") @PathVariable Long fileId,
            @Parameter(description = "New description") @RequestParam(required = false) String description,
            @Parameter(description = "Make file public") @RequestParam(required = false) Boolean isPublic) {
        
        log.info("PUT /api/v1/files/{} - Updating file metadata", fileId);
        
        FileMetadataDTO updatedFile = fileManagementService.updateFileMetadata(fileId, description, isPublic);
        
        return ResponseEntity.ok(ApiResponse.success("File metadata updated successfully", updatedFile));
    }
    
    @DeleteMapping("/{fileId}")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Delete file", description = "Delete a file and its metadata")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @Parameter(description = "File ID") @PathVariable Long fileId) {
        
        log.info("DELETE /api/v1/files/{} - Deleting file", fileId);
        
        fileManagementService.deleteFile(fileId);
        
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }
    
    // ==================== ADMIN ENDPOINTS ====================
    
    @GetMapping("/admin/all")
    @PreAuthorize(SecurityConstants.HAS_ADMIN_ROLE)
    @Operation(summary = "Get all files", description = "Admin endpoint to get all files in the system")
    public ResponseEntity<ApiResponse<Page<FileMetadataDTO>>> getAllFiles(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("GET /api/v1/files/admin/all - Admin getting all files");
        
        // TODO: Implement admin file listing
        
        return ResponseEntity.ok(ApiResponse.success("All files retrieved successfully", Page.empty()));
    }
}
