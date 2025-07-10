package com.fix4home.fix4home.model.dto.servicepost;

import com.fix4home.fix4home.model.dto.customer.AddressDTO;
import com.fix4home.fix4home.model.dto.service.ServiceDTO;
import com.fix4home.fix4home.model.enums.ServicePostStatus;
import com.fix4home.fix4home.model.enums.ServicePostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePostDTO {
    
    private Long id;
    private Long customerId;
    private String customerName;
    private ServiceDTO service;
    private AddressDTO address;
    private String title;
    private String description;
    private BigDecimal estimatedBudget;
    private LocalDateTime preferredTime;
    private ServicePostType type;
    private ServicePostStatus status;
    private Integer maxTechnicians;
    private LocalDateTime expiresAt;
    private Long selectedTechnicianId;
    private String selectedTechnicianName;
    private LocalDateTime selectedAt;
    private BigDecimal finalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer responseCount;
    private List<ServicePostResponseDTO> responses;
    
    // Computed fields
    private Boolean isActive;
    private Boolean canReceiveResponses;
    private Boolean isExpired;
} 