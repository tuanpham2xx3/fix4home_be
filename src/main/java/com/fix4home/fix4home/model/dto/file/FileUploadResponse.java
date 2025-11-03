package com.fix4home.fix4home.model.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "File upload response")
public class FileUploadResponse {
    
    @Schema(description = "Successfully uploaded files")
    private List<FileMetadataDTO> uploadedFiles;
    
    @Schema(description = "Failed uploads with error messages")
    private List<FileUploadError> errors;
    
    @Schema(description = "Total number of files processed")
    private Integer totalFiles;
    
    @Schema(description = "Number of successful uploads")
    private Integer successCount;
    
    @Schema(description = "Number of failed uploads")
    private Integer errorCount;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "File upload error information")
    public static class FileUploadError {
        
        @Schema(description = "Original filename")
        private String filename;
        
        @Schema(description = "Error message")
        private String error;
        
        @Schema(description = "Error code")
        private String errorCode;
    }
}
