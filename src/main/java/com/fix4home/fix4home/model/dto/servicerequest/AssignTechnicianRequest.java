package com.fix4home.fix4home.model.dto.servicerequest;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignTechnicianRequest {
    
    @NotNull(message = "Technician ID is required")
    private Long technicianId;
    
    private LocalDateTime scheduledTime;
} 