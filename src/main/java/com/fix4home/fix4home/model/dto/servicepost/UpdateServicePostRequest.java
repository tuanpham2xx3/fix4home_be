package com.fix4home.fix4home.model.dto.servicepost;

import com.fix4home.fix4home.model.enums.ServicePostStatus;
import com.fix4home.fix4home.model.enums.ServicePostType;
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
public class UpdateServicePostRequest {
    
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;
    
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Estimated budget must be positive or zero")
    @Digits(integer = 10, fraction = 2, message = "Estimated budget must have at most 10 integer digits and 2 decimal places")
    private BigDecimal estimatedBudget;
    
    private LocalDateTime preferredTime;
    
    private ServicePostType type;
    
    private ServicePostStatus status;
    
    @Min(value = 1, message = "Max technicians must be at least 1")
    @Max(value = 50, message = "Max technicians cannot exceed 50")
    private Integer maxTechnicians;
    
    private LocalDateTime expiresAt;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Final price must be positive or zero")
    @Digits(integer = 10, fraction = 2, message = "Final price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal finalPrice;
} 