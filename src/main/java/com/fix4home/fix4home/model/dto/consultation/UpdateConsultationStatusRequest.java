package com.fix4home.fix4home.model.dto.consultation;

import com.fix4home.fix4home.model.enums.ConsultationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateConsultationStatusRequest {
    
    @NotNull(message = "Status is required")
    private ConsultationStatus status;
    
    private String rejectionReason; // Optional reason for rejection
} 