package com.fix4home.fix4home.model.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectTechnicianRequest {
    
    @NotBlank(message = "Rejection reason is required")
    @Size(min = 10, max = 500, message = "Rejection reason must be between 10 and 500 characters")
    private String rejectionReason; // Lý do từ chối (bắt buộc)
} 