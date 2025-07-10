package com.fix4home.fix4home.model.dto.servicepost;

import com.fix4home.fix4home.model.enums.ServicePostStatus;
import com.fix4home.fix4home.model.enums.ServicePostType;
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
public class ServicePostSummaryDTO {
    
    private Long id;
    private String title;
    private String serviceName;
    private String address;
    private BigDecimal estimatedBudget;
    private LocalDateTime preferredTime;
    private ServicePostType type;
    private ServicePostStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Integer responseCount;
    private Boolean isActive;
    private Boolean isExpired;
    
    // For customer view
    private String customerName;
    
    // For technician view - show if they already responded
    private Boolean hasResponded;
} 