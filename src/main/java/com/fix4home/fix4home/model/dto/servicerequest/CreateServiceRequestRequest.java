package com.fix4home.fix4home.model.dto.servicerequest;

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
public class CreateServiceRequestRequest {
    
    @NotNull(message = "Service ID is required")
    private Long serviceId;
    
    @NotNull(message = "Address ID is required")
    private Long addressId;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;
    
    @Future(message = "Scheduled time must be in the future")
    private LocalDateTime scheduledTime;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal price;
} 