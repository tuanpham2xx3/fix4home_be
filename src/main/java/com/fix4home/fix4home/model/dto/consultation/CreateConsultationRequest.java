package com.fix4home.fix4home.model.dto.consultation;

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
public class CreateConsultationRequest {
    
    @NotNull(message = "Service post ID is required")
    @Positive(message = "Service post ID must be positive")
    private Long servicePostId;
    
    @NotBlank(message = "Proposal is required")
    @Size(min = 10, max = 2000, message = "Proposal must be between 10 and 2000 characters")
    private String proposal;
    
    @NotNull(message = "Quoted price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Quoted price must be positive or zero")
    @Digits(integer = 10, fraction = 2, message = "Quoted price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal quotedPrice;
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
} 