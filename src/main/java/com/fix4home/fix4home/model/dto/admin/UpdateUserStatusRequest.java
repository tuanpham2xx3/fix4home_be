package com.fix4home.fix4home.model.dto.admin;

import com.fix4home.fix4home.model.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserStatusRequest {
    
    @NotNull(message = "Status is required")
    private UserStatus status;
    
    private String reason;
} 