package com.fix4home.fix4home.model.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressValidationResult {
    
    private Boolean valid;
    private String message;
    private WardDTO wardData;
    
    public static AddressValidationResult valid(WardDTO wardData) {
        return AddressValidationResult.builder()
                .valid(true)
                .message("Address is valid")
                .wardData(wardData)
                .build();
    }
    
    public static AddressValidationResult invalid(String message) {
        return AddressValidationResult.builder()
                .valid(false)
                .message(message)
                .build();
    }
} 