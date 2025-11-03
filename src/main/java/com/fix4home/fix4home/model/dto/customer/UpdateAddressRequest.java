package com.fix4home.fix4home.model.dto.customer;

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
public class UpdateAddressRequest {
    
    @Size(min = 2, max = 100, message = "Recipient name must be between 2 and 100 characters")
    private String recipientName;
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be 10-15 digits, optionally starting with +")
    private String recipientPhone;
    
    @Size(max = 255, message = "Address line cannot exceed 255 characters")
    private String addressLine;
    
    @Size(max = 100, message = "Ward cannot exceed 100 characters")
    private String ward;
    
    @Size(max = 100, message = "District cannot exceed 100 characters")
    private String district;
    
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;
    
    // Vietnam Administrative API integration fields (optional for validation)
    @Size(max = 10, message = "Province code cannot exceed 10 characters")
    private String provinceCode;
    
    @Size(max = 10, message = "Ward code cannot exceed 10 characters")
    private String wardCode;
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Digits(integer = 3, fraction = 6, message = "Latitude must have at most 3 integer digits and 6 decimal places")
    private BigDecimal latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Digits(integer = 3, fraction = 6, message = "Longitude must have at most 3 integer digits and 6 decimal places")
    private BigDecimal longitude;
} 