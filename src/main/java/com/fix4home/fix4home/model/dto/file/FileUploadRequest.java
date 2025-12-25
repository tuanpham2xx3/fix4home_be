package com.fix4home.fix4home.model.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "File upload request with metadata")
public class FileUploadRequest {
    
    @Schema(description = "Entity type this file belongs to", example = "SERVICE_REQUEST")
    private String entityType;
    
    @Schema(description = "Entity ID this file belongs to", example = "123")
    private Long entityId;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "File description", example = "Before repair photo")
    private String description;
    
    @Schema(description = "Whether file should be publicly accessible", example = "true")
    @Builder.Default
    private Boolean isPublic = true;
}
