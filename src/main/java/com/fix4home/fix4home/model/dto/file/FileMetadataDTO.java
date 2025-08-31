package com.fix4home.fix4home.model.dto.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fix4home.fix4home.model.enums.FileType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "File metadata information")
public class FileMetadataDTO {
    
    @Schema(description = "File ID", example = "1")
    private Long id;
    
    @Schema(description = "Original filename", example = "house_repair.jpg")
    private String originalFilename;
    
    @Schema(description = "Stored filename", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479.jpg")
    private String storedFilename;
    
    @Schema(description = "File download URL", example = "http://localhost:8100/api/v1/files/download/f47ac10b-58cc-4372-a567-0e02b2c3d479.jpg")
    private String fileUrl;
    
    @Schema(description = "File content type", example = "image/jpeg")
    private String contentType;
    
    @Schema(description = "File size in bytes", example = "1048576")
    private Long fileSize;
    
    @Schema(description = "File size formatted", example = "1.0 MB")
    private String fileSizeFormatted;
    
    @Schema(description = "File type category")
    private FileType fileType;
    
    @Schema(description = "User who uploaded the file")
    private String uploadedBy;
    
    @Schema(description = "Entity type this file belongs to", example = "SERVICE_REQUEST")
    private String entityType;
    
    @Schema(description = "Entity ID this file belongs to", example = "123")
    private Long entityId;
    
    @Schema(description = "File description", example = "Before repair photo")
    private String description;
    
    @Schema(description = "Whether file is publicly accessible")
    private Boolean isPublic;
    
    @Schema(description = "Thumbnail URL for images")
    private String thumbnailUrl;
    
    @Schema(description = "Image width in pixels")
    private Integer width;
    
    @Schema(description = "Image height in pixels")
    private Integer height;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Upload timestamp")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
