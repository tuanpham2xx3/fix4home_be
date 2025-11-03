package com.fix4home.fix4home.model.dto.servicepost;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateServicePostResponseRequest {
    
    @NotBlank(message = "Message is required")
    @Size(min = 10, max = 1000, message = "Message must be between 10 and 1000 characters")
    private String message;
    
    @NotNull(message = "Quoted price is required")
    @DecimalMin(value = "0.01", message = "Quoted price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Quoted price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal quotedPrice;
    
    @Min(value = 15, message = "Estimated duration must be at least 15 minutes")
    @Max(value = 2880, message = "Estimated duration cannot exceed 48 hours (2880 minutes)")
    private Integer estimatedDuration; // in minutes
    
    @Future(message = "Proposed time must be in the future")
    private LocalDateTime proposedTime;
} 