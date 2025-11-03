package com.fix4home.fix4home.model.dto.technician;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStatusRequest {
    
    @NotNull(message = "Online status is required")
    private Boolean isOnline;
} 