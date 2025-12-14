package com.fix4home.fix4home.service;

import com.fix4home.fix4home.config.FileStorageConfig;
import com.fix4home.fix4home.exception.BusinessValidationException;
import com.fix4home.fix4home.exception.FileStorageException;
import com.fix4home.fix4home.exception.ResourceNotFoundException;
import com.fix4home.fix4home.model.dto.file.FileMetadataDTO;
import com.fix4home.fix4home.model.dto.file.FileUploadRequest;
import com.fix4home.fix4home.model.dto.file.FileUploadResponse;
import com.fix4home.fix4home.model.entity.FileMetadata;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.FileType;
import com.fix4home.fix4home.repository.FileMetadataRepository;
import com.fix4home.fix4home.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileManagementService extends BaseService {
    
    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorageService fileStorageService;
    private final ImageProcessingService imageProcessingService;
    private final FileStorageConfig fileStorageConfig;
    private final Tika tika = new Tika();
    
    /**
     * Upload multiple files
     */
    public FileUploadResponse uploadFiles(MultipartFile[] files, FileUploadRequest request) {
        logBusinessOperation("UPLOAD_FILES", "files=" + files.length + ", entityType=" + request.getEntityType());
        
        User currentUser = getCurrentUser();
        
        List<FileMetadataDTO> uploadedFiles = new ArrayList<>();
        List<FileUploadResponse.FileUploadError> errors = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                FileMetadataDTO uploadedFile = uploadSingleFile(file, request, currentUser);
                uploadedFiles.add(uploadedFile);
            } catch (Exception ex) {
                log.error("Error uploading file: {}", file.getOriginalFilename(), ex);
                errors.add(FileUploadResponse.FileUploadError.builder()
                        .filename(file.getOriginalFilename())
                        .error(ex.getMessage())
                        .errorCode("UPLOAD_FAILED")
                        .build());
            }
        }
        
        return FileUploadResponse.builder()
                .uploadedFiles(uploadedFiles)
                .errors(errors)
                .totalFiles(files.length)
                .successCount(uploadedFiles.size())
                .errorCount(errors.size())
                .build();
    }
    
    /**
     * Upload single file
     */
    public FileMetadataDTO uploadSingleFile(MultipartFile file, FileUploadRequest request, User user) {
        // Validate file
        validateFile(file);
        
        try {
            // Store file
            String directory = determineStorageDirectory(request.getEntityType());
            String storedFilename = fileStorageService.storeFile(file, directory);
            
            // Detect content type
            String contentType = detectContentType(file);
            FileType fileType = FileType.fromContentType(contentType);
            
            // Create file metadata
            FileMetadata fileMetadata = FileMetadata.builder()
                    .originalFilename(StringUtils.cleanPath(file.getOriginalFilename()))
                    .storedFilename(storedFilename)
                    .filePath(fileStorageService.getFilePath(storedFilename).toString())
                    .fileUrl(fileStorageService.getFileUrl(storedFilename))
                    .contentType(contentType)
                    .fileSize(file.getSize())
                    .fileType(fileType)
                    .uploadedBy(user)
                    .entityType(request.getEntityType())
                    .entityId(request.getEntityId())
                    .description(request.getDescription())
                    .isPublic(request.getIsPublic())
                    .build();
            
            // Process image if needed
            if (fileType.isImageType()) {
                processImage(fileMetadata);
            }
            
            // Save metadata
            fileMetadata = fileMetadataRepository.save(fileMetadata);
            
            log.info("File uploaded successfully: {} -> {}", file.getOriginalFilename(), storedFilename);
            
            return convertToDTO(fileMetadata);
            
        } catch (IOException ex) {
            throw new FileStorageException("Failed to upload file: " + file.getOriginalFilename(), ex);
        }
    }
    
    /**
     * Download file
     */
    @Transactional(readOnly = true)
    public Resource downloadFile(String filename) {
        logBusinessOperation("DOWNLOAD_FILE", "filename=" + filename);
        
        FileMetadata fileMetadata = fileMetadataRepository.findByStoredFilename(filename)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + filename));
        
        // Check access permissions
        if (!canAccessFile(fileMetadata)) {
            throw new BusinessValidationException("Access denied to file: " + filename);
        }
        
        try {
            return fileStorageService.loadFileAsResource(filename);
        } catch (IOException ex) {
            throw new FileStorageException("Could not download file: " + filename, ex);
        }
    }
    
    /**
     * Get file metadata by ID
     */
    @Transactional(readOnly = true)
    public FileMetadataDTO getFileMetadata(Long fileId) {
        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + fileId));
        
        if (!canAccessFile(fileMetadata)) {
            throw new BusinessValidationException("Access denied to file: " + fileId);
        }
        
        return convertToDTO(fileMetadata);
    }
    
    /**
     * Get file metadata by stored filename
     */
    @Transactional(readOnly = true)
    public FileMetadataDTO getFileMetadataByFilename(String filename) {
        FileMetadata fileMetadata = fileMetadataRepository.findByStoredFilename(filename)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + filename));
        
        if (!canAccessFile(fileMetadata)) {
            throw new BusinessValidationException("Access denied to file: " + filename);
        }
        
        return convertToDTO(fileMetadata);
    }
    
    /**
     * Get files by entity
     */
    @Transactional(readOnly = true)
    public List<FileMetadataDTO> getFilesByEntity(String entityType, Long entityId) {
        List<FileMetadata> files = fileMetadataRepository.findByEntityTypeAndEntityIdOrderByCreatedAtAsc(entityType, entityId);
        
        return files.stream()
                .filter(this::canAccessFile)
                .map(this::convertToDTO)
                .toList();
    }
    
    /**
     * Get user avatar URL (latest avatar uploaded for the user)
     * Returns the URL of the most recently uploaded avatar for the user.
     * Access control is handled at the file download/view level.
     * 
     * @param userId User ID
     * @return Avatar URL or null if not found
     */
    @Transactional(readOnly = true)
    public String getUserAvatarUrl(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            // Get the latest avatar for the user (most recently uploaded)
            Optional<FileMetadata> avatarOpt = fileMetadataRepository
                    .findFirstByEntityTypeAndEntityIdOrderByCreatedAtDesc("USER_AVATAR", userId);
            
            // Return the file URL if avatar exists, null otherwise
            // Access control is handled at the file download/view endpoint level
            return avatarOpt.map(FileMetadata::getFileUrl).orElse(null);
            
        } catch (Exception e) {
            log.warn("Error getting user avatar URL for user {}: {}", userId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user's files
     */
    @Transactional(readOnly = true)
    public Page<FileMetadataDTO> getUserFiles(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<FileMetadata> files = fileMetadataRepository.findByUploadedByOrderByCreatedAtDesc(currentUser, pageable);
        
        return files.map(this::convertToDTO);
    }
    
    /**
     * Get images by type with optional public filter
     */
    @Transactional(readOnly = true)
    public Page<FileMetadataDTO> getImagesByType(Boolean isPublic, Pageable pageable) {
        Page<FileMetadata> images;
        
        if (isPublic != null && isPublic) {
            // Get only public images
            images = fileMetadataRepository.findWithCriteria(
                    null, // user
                    FileType.IMAGE, // fileType
                    null, // entityType
                    true, // isPublic
                    null, // keyword
                    pageable
            );
        } else {
            // Get all images (with access control)
            images = fileMetadataRepository.findByFileTypeOrderByCreatedAtDesc(FileType.IMAGE, pageable);
        }
        
        // Convert to DTO and filter by access permissions
        // Note: We filter after conversion, but this may not preserve pagination perfectly
        // For better performance, consider filtering at database level
        List<FileMetadataDTO> filteredDTOs = images.getContent().stream()
                .filter(this::canAccessFile)
                .map(this::convertToDTO)
                .toList();
        
        // Create a new page with filtered content
        // Note: This approach doesn't preserve total count accurately for filtered results
        // For production, consider implementing database-level filtering
        return new org.springframework.data.domain.PageImpl<>(
                filteredDTOs,
                pageable,
                isPublic != null && isPublic ? images.getTotalElements() : images.getTotalElements()
        );
    }
    
    /**
     * Delete file
     */
    public void deleteFile(Long fileId) {
        logBusinessOperation("DELETE_FILE", "fileId=" + fileId);
        
        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + fileId));
        
        // Check permissions
        User currentUser = getCurrentUser();
        if (!fileMetadata.getUploadedBy().getId().equals(currentUser.getId()) && !SecurityHelper.isAdmin(currentUser)) {
            throw new BusinessValidationException("You can only delete your own files");
        }
        
        // Delete physical file
        boolean deleted = fileStorageService.deleteFile(fileMetadata.getStoredFilename());
        if (!deleted) {
            log.warn("Physical file not found or could not be deleted: {}", fileMetadata.getStoredFilename());
        }
        
        // Delete thumbnail if exists
        if (fileMetadata.getThumbnailUrl() != null) {
            String thumbnailFilename = "thumb_" + fileMetadata.getStoredFilename();
            fileStorageService.deleteFile(thumbnailFilename);
        }
        
        // Delete metadata
        fileMetadataRepository.delete(fileMetadata);
        
        log.info("File deleted successfully: {}", fileMetadata.getOriginalFilename());
    }
    
    /**
     * Update file metadata
     */
    public FileMetadataDTO updateFileMetadata(Long fileId, String description, Boolean isPublic) {
        logBusinessOperation("UPDATE_FILE_METADATA", "fileId=" + fileId);
        
        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + fileId));
        
        // Check permissions - owner or admin can update
        User currentUser = SecurityHelper.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessValidationException("Authentication required to update file");
        }
        
        boolean isOwner = fileMetadata.getUploadedBy().getId().equals(currentUser.getId());
        boolean isAdmin = SecurityHelper.isAdmin(currentUser);
        
        if (!isOwner && !isAdmin) {
            throw new BusinessValidationException("You can only update your own files");
        }
        
        // Update metadata
        if (description != null) {
            fileMetadata.setDescription(description);
        }
        if (isPublic != null) {
            fileMetadata.setIsPublic(isPublic);
        }
        
        fileMetadata = fileMetadataRepository.save(fileMetadata);
        
        log.info("File metadata updated by {} (owner: {}, admin: {}): fileId={}, isPublic={}", 
                currentUser.getUsername(), isOwner, isAdmin, fileId, isPublic);
        
        return convertToDTO(fileMetadata);
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessValidationException("File is empty");
        }
        
        if (file.getSize() > fileStorageConfig.getMaxFileSize()) {
            throw new BusinessValidationException("File size exceeds maximum allowed size: " + 
                    (fileStorageConfig.getMaxFileSize() / 1024 / 1024) + " MB");
        }
        
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        if (filename.contains("..")) {
            throw new BusinessValidationException("Filename contains invalid path sequence: " + filename);
        }
        
        String extension = getFileExtension(filename);
        if (!fileStorageConfig.getAllowedExtensions().contains(extension.toLowerCase())) {
            throw new BusinessValidationException("File extension not allowed: " + extension + 
                    ". Allowed extensions: " + String.join(", ", fileStorageConfig.getAllowedExtensions()));
        }
    }
    
    private String detectContentType(MultipartFile file) {
        try {
            return tika.detect(file.getInputStream(), file.getOriginalFilename());
        } catch (IOException ex) {
            log.warn("Could not detect content type for file: {}", file.getOriginalFilename());
            return file.getContentType();
        }
    }
    
    private String determineStorageDirectory(String entityType) {
        if (entityType == null) {
            return "general";
        }
        
        return switch (entityType.toUpperCase()) {
            case "SERVICE_REQUEST" -> "service-requests";
            case "SERVICE_POST" -> "service-posts";
            case "TECHNICIAN_PROFILE" -> "technician-profiles";
            case "USER_AVATAR" -> "avatars";
            default -> "general";
        };
    }
    
    private void processImage(FileMetadata fileMetadata) {
        try {
            Path imagePath = fileStorageService.getFilePath(fileMetadata.getStoredFilename());
            
            // Get image dimensions
            ImageProcessingService.ImageDimensions dimensions = imageProcessingService.getImageDimensions(imagePath);
            if (dimensions != null) {
                fileMetadata.setWidth(dimensions.getWidth());
                fileMetadata.setHeight(dimensions.getHeight());
            }
            
            // Resize if needed
            imageProcessingService.resizeImageIfNeeded(imagePath, fileMetadata.getStoredFilename());
            
            // Create thumbnail
            String thumbnailPath = imageProcessingService.createThumbnail(imagePath, fileMetadata.getStoredFilename());
            if (thumbnailPath != null) {
                String thumbnailFilename = "thumb_" + fileMetadata.getStoredFilename();
                fileMetadata.setThumbnailUrl(fileStorageService.getFileUrl(thumbnailFilename));
            }
            
        } catch (Exception ex) {
            log.error("Error processing image: {}", fileMetadata.getOriginalFilename(), ex);
            // Don't fail the upload, just log the error
        }
    }
    
    private boolean canAccessFile(FileMetadata fileMetadata) {
        // Public files are accessible to everyone (including unauthenticated users)
        if (Boolean.TRUE.equals(fileMetadata.getIsPublic())) {
            return true;
        }
        
        // Try to get current user (may be null if not authenticated)
        User currentUser = null;
        try {
            currentUser = SecurityHelper.getCurrentUser();
        } catch (Exception e) {
            // No authenticated user - only public files are accessible
            log.debug("No authenticated user for file access check: {}", fileMetadata.getStoredFilename());
        }
        
        // If no authenticated user, only public files are accessible
        if (currentUser == null) {
            log.debug("Access denied: File {} is not public and no user is authenticated", fileMetadata.getStoredFilename());
            return false;
        }
        
        // File owner can always access
        if (fileMetadata.getUploadedBy().getId().equals(currentUser.getId())) {
            return true;
        }
        
        // Admin can access all files
        if (SecurityHelper.isAdmin(currentUser)) {
            return true;
        }
        
        // TODO: Add entity-specific access rules (e.g., participants in service request can access files)
        
        log.debug("Access denied: User {} does not have permission to access file {}", 
                currentUser.getUsername(), fileMetadata.getStoredFilename());
        return false;
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1);
    }
    
    private FileMetadataDTO convertToDTO(FileMetadata fileMetadata) {
        return FileMetadataDTO.builder()
                .id(fileMetadata.getId())
                .originalFilename(fileMetadata.getOriginalFilename())
                .storedFilename(fileMetadata.getStoredFilename())
                .fileUrl(fileMetadata.getFileUrl())
                .contentType(fileMetadata.getContentType())
                .fileSize(fileMetadata.getFileSize())
                .fileSizeFormatted(fileMetadata.getFileSizeFormatted())
                .fileType(fileMetadata.getFileType())
                .uploadedBy(fileMetadata.getUploadedBy().getUsername())
                .entityType(fileMetadata.getEntityType())
                .entityId(fileMetadata.getEntityId())
                .description(fileMetadata.getDescription())
                .isPublic(fileMetadata.getIsPublic())
                .thumbnailUrl(fileMetadata.getThumbnailUrl())
                .width(fileMetadata.getWidth())
                .height(fileMetadata.getHeight())
                .createdAt(fileMetadata.getCreatedAt())
                .updatedAt(fileMetadata.getUpdatedAt())
                .build();
    }
}
