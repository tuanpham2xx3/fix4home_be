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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePostSearchResultDTO {
    
    // Basic info
    private Long id;
    private String title;
    private String description;
    private ServicePostType type;
    private ServicePostStatus status;
    
    // Customer info
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Double customerRating;
    
    // Service info
    private ServiceDTO service;
    
    // Location info
    private AddressDTO address;
    private Double distanceKm; // Distance from search location
    
    // Pricing
    private BigDecimal estimatedBudget;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    
    // Timing
    private LocalDateTime preferredTime;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Integer urgencyHours; // How urgent (hours until preferred time)
    
    // Response info
    private Integer responseCount;
    private Integer maxTechnicians;
    private Boolean hasResponded; // If current technician has responded
    
    // Search relevance
    private Double relevanceScore; // How well this post matches the search criteria
    
    // Urgency indicators
    private Boolean isUrgent;
    private Boolean isExpiringSoon; // Expires within 24 hours
    
    // Helper methods
    public boolean isAvailable() {
        return status == ServicePostStatus.POSTED || status == ServicePostStatus.RESPONSES_RECEIVED;
    }
    
    public boolean isNearby(double maxDistanceKm) {
        return distanceKm != null && distanceKm <= maxDistanceKm;
    }
    
    public boolean isExpiring() {
        if (expiresAt == null) return false;
        return expiresAt.isBefore(LocalDateTime.now().plusHours(24));
    }
    
    public boolean canRespond() {
        return isAvailable() && !Boolean.TRUE.equals(hasResponded) && 
               (maxTechnicians == null || responseCount < maxTechnicians);
    }
} 