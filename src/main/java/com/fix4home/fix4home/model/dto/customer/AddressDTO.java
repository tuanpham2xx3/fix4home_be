package com.fix4home.fix4home.model.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    
    private Long id;
    private Long userId;
    private String recipientName;
    private String recipientPhone;
    private String addressLine;
    private String ward;
    private String district;
    private String city;
    
    // Vietnam Administrative API integration fields
    private String provinceCode;
    private String wardCode;
    
    private BigDecimal latitude;
    private BigDecimal longitude;
} 