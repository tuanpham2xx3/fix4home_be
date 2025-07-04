package com.fix4home.fix4home.model.dto.servicerequest;

import com.fix4home.fix4home.model.dto.customer.AddressDTO;
import com.fix4home.fix4home.model.dto.service.ServiceDTO;
import com.fix4home.fix4home.model.enums.ServiceRequestStatus;
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
public class ServiceRequestDTO {
    
    private Long id;
    private ServiceRequestStatus status;
    private String description;
    private LocalDateTime scheduledTime;
    private LocalDateTime completedTime;
    private LocalDateTime createdAt;
    private BigDecimal price;
    
    // Customer information
    private CustomerSummaryDTO customer;
    
    // Service information
    private ServiceDTO service;
    
    // Technician information (nullable)
    private TechnicianSummaryDTO technician;
    
    // Address information
    private AddressDTO address;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerSummaryDTO {
        private Long userId;
        private String username;
        private String email;
        private String phoneNumber;
        private String fullName;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TechnicianSummaryDTO {
        private Long userId;
        private String username;
        private String email;
        private String phoneNumber;
        private String fullName;
        private Float rating;
    }
} 