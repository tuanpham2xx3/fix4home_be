package com.fix4home.fix4home.model.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressValidationRequest {
    
    private String provinceCode;
    private String wardCode;
} 