package com.fix4home.fix4home.model.dto.servicerequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestStatsDTO {
    
    private long totalRequests;
    private long pendingRequests;
    private long assignedRequests;
    private long inProgressRequests;
    private long completedRequests;
    private long cancelledRequests;
    
    private BigDecimal totalRevenue;
    private BigDecimal averagePrice;
    
    private long activeCustomers;
    private long activeTechnicians;
    
    private double completionRate;
    private double cancellationRate;
} 