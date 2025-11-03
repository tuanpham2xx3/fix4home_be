package com.fix4home.fix4home.model.dto.common;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSearchRequest {
    
    // Text search
    private String keyword;
    
    // Location-based search
    private String location; // Text-based location (address, city, district)
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
    
    @Min(value = 1, message = "Radius must be at least 1 km")
    @Max(value = 100, message = "Radius cannot exceed 100 km")
    @Builder.Default
    private Integer radius = 10; // Search radius in kilometers
    
    // Price filtering
    @DecimalMin(value = "0.0", message = "Minimum price must be positive")
    private BigDecimal minPrice;
    
    @DecimalMin(value = "0.0", message = "Maximum price must be positive")
    private BigDecimal maxPrice;
    
    // Rating filtering
    @DecimalMin(value = "0.0", message = "Minimum rating must be between 0 and 5")
    @DecimalMax(value = "5.0", message = "Minimum rating must be between 0 and 5")
    private Double minRating;
    
    @DecimalMin(value = "0.0", message = "Maximum rating must be between 0 and 5")
    @DecimalMax(value = "5.0", message = "Maximum rating must be between 0 and 5")
    private Double maxRating;
    
    // Skills filtering
    private List<Long> skillIds;
    
    // Availability filtering
    private Boolean availableNow; // Only online technicians
    private Boolean hasLocation; // Only technicians with location data
    
    // Service filtering
    private List<Long> serviceIds;
    
    // Experience filtering
    @Min(value = 0, message = "Minimum experience must be non-negative")
    private Integer minExperienceYears;
    
    // Sorting
    @Builder.Default
    private String sortBy = "distance"; // distance, rating, price, experience, name
    
    @Builder.Default
    private String sortDirection = "asc"; // asc, desc
    
    // Pagination
    @Min(value = 0, message = "Page must be non-negative")
    @Builder.Default
    private Integer page = 0;
    
    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size cannot exceed 100")
    @Builder.Default
    private Integer size = 20;
    
    // Helper methods
    public boolean hasLocationFilter() {
        return latitude != null && longitude != null;
    }
    
    public boolean hasPriceFilter() {
        return minPrice != null || maxPrice != null;
    }
    
    public boolean hasRatingFilter() {
        return minRating != null || maxRating != null;
    }
    
    public boolean hasSkillsFilter() {
        return skillIds != null && !skillIds.isEmpty();
    }
    
    public boolean hasServiceFilter() {
        return serviceIds != null && !serviceIds.isEmpty();
    }
    
    public boolean hasTextSearch() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    public boolean hasLocationTextSearch() {
        return location != null && !location.trim().isEmpty();
    }
    
    // Validation method
    public boolean isValid() {
        // If coordinates are provided, both must be present
        if ((latitude != null && longitude == null) || (latitude == null && longitude != null)) {
            return false;
        }
        
        // Price range validation
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            return false;
        }
        
        // Rating range validation
        if (minRating != null && maxRating != null && minRating > maxRating) {
            return false;
        }
        
        return true;
    }
} 