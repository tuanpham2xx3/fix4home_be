package com.fix4home.fix4home.model.dto.servicerequest;

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
public class ServiceRequestSummaryDTO {
    
    private Long id;
    private ServiceRequestStatus status;
    private String description;
    private LocalDateTime scheduledTime;
    private LocalDateTime createdAt;
    private BigDecimal price;
    
    // Basic customer info
    private String customerName;
    private String customerPhone;
    
    // Basic service info
    private String serviceName;
    private String serviceCategory;
    
    // Basic technician info (nullable)
    private String technicianName;
    private Float technicianRating;
    
    // Basic address info
    private String address;
    private String city;
} 