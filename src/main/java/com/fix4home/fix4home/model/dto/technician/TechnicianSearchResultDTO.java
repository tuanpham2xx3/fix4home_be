package com.fix4home.fix4home.model.dto.technician;

import com.fix4home.fix4home.model.dto.technician.SkillDTO;
import com.fix4home.fix4home.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianSearchResultDTO {
    
    // Basic info
    private Long userId;
    private Long profileId;
    private String fullName;
    private String email;
    private String phoneNumber;
    
    // Professional info
    private Double rating;
    private Integer totalJobs;
    private Integer experienceYears;
    private UserStatus status;
    
    // Location info
    private Double currentLatitude;
    private Double currentLongitude;
    private String currentAddress;
    private Integer workingRadius;
    private Double distanceKm; // Distance from search location
    
    // Availability
    private Boolean isOnline;
    private LocalDateTime lastSeenAt;
    
    // Skills
    private List<SkillDTO> skillList;
    
    // Pricing (if available)
    private BigDecimal averagePrice;
    private BigDecimal basePrice;
    
    // Search relevance
    private Double relevanceScore; // How well this technician matches the search criteria
    
    // Profile details
    private String profileImageUrl;
    private String description;
    
    // Statistics
    private Integer completedJobs;
    private Integer totalReviews;
    
    // Helper methods
    public boolean isAvailable() {
        return Boolean.TRUE.equals(isOnline) && status == UserStatus.ACTIVE;
    }
    
    public boolean isNearby(double maxDistanceKm) {
        return distanceKm != null && distanceKm <= maxDistanceKm;
    }
} 