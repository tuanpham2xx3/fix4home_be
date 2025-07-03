package com.fix4home.fix4home.model.dto.service;

import com.fix4home.fix4home.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceDTO {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private UserStatus status;
} 