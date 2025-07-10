package com.fix4home.fix4home.model.dto.complaint;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateComplaintRequest {
    
    @NotNull(message = "Service request ID is required")
    @Positive(message = "Service request ID must be positive")
    private Long serviceRequestId;
    
    @NotNull(message = "Accused user ID is required")
    @Positive(message = "Accused user ID must be positive")
    private Long accusedId;
    
    @NotBlank(message = "Complaint reason is required")
    @Size(min = 10, max = 200, message = "Reason must be between 10 and 200 characters")
    private String reason;
    
    @NotBlank(message = "Complaint description is required")
    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters")
    private String description;
} 