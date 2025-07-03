package com.fix4home.fix4home.model.dto.service;

import com.fix4home.fix4home.model.enums.UserStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateServiceRequest {
    
    @Size(min = 2, max = 100, message = "Service name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Base price must be positive or zero")
    @Digits(integer = 10, fraction = 2, message = "Base price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal basePrice;
    
    private UserStatus status;
} 