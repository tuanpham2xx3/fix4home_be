package com.fix4home.fix4home.model.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EnhancedSearchRequest extends ServiceSearchRequest {
    
    // ==================== ENHANCED FILTERS ====================
    
    // Multi-select service categories
    private List<String> serviceCategories;
    
    // Price range with more granular control
    @DecimalMin(value = "0.0", message = "Price step must be positive")
    private BigDecimal priceStep; // For price range sliders
    
    // Advanced rating filters
    @Min(value = 0, message = "Minimum reviews must be non-negative")
    private Integer minReviewCount;
    
    @DecimalMin(value = "0.0", message = "Minimum completion rate must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Minimum completion rate must be between 0 and 1")
    private Double minCompletionRate;
    
    // Time-based availability filters
    @JsonFormat(pattern = "HH:mm")
    private LocalTime availableFromTime;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime availableToTime;
    
    private List<String> availableDays; // MON, TUE, WED, THU, FRI, SAT, SUN
    
    // Advanced location filters
    @Min(value = 1, message = "Minimum working radius must be at least 1 km")
    private Integer minWorkingRadius;
    
    private List<String> workingAreas; // Specific districts or areas
    
    // Experience and certification filters
    private List<String> certifications;
    
    @Min(value = 0, message = "Maximum experience must be non-negative")
    private Integer maxExperienceYears;
    
    private Boolean verifiedTechnician; // Only verified/approved technicians
    
    // Response time filters
    @Min(value = 1, message = "Maximum response hours must be at least 1")
    private Integer maxResponseHours;
    
    // Service-specific filters
    private Boolean emergencyService; // Supports emergency/urgent services
    private Boolean warrantyOffered; // Offers warranty on work
    private Boolean insuranceCovered; // Has insurance coverage
    
    // Search behavior
    private Boolean fuzzySearch; // Enable fuzzy/approximate matching
    
    @DecimalMin(value = "0.0", message = "Similarity threshold must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Similarity threshold must be between 0 and 1")
    private Double similarityThreshold = 0.7; // For fuzzy search
    
    private Boolean includeInactive; // Include inactive but not banned technicians
    
    // Search presets
    private String searchPreset; // 'EMERGENCY', 'HIGH_VALUE', 'NEARBY', 'TOP_RATED', 'BUDGET_FRIENDLY'
    
    // Advanced sorting options
    private List<String> sortPriority; // Multiple sort criteria in order of priority
    
    private Boolean boostNearby; // Boost nearby results in relevance scoring
    private Boolean boostTopRated; // Boost top-rated results in relevance scoring
    private Boolean boostRecentlyActive; // Boost recently active technicians
    
    // Search metadata
    private Boolean trackSearch; // Whether to save this search in history
    private String searchContext; // Context where search is performed (web, mobile, etc.)
    
    // ==================== VALIDATION HELPERS ====================
    
    public boolean hasAdvancedFilters() {
        return hasServiceCategoryFilter() || hasTimeAvailabilityFilter() || 
               hasCertificationFilter() || hasResponseTimeFilter() || 
               hasWorkingAreaFilter() || hasAdvancedRatingFilter();
    }
    
    public boolean hasServiceCategoryFilter() {
        return serviceCategories != null && !serviceCategories.isEmpty();
    }
    
    public boolean hasTimeAvailabilityFilter() {
        return availableFromTime != null || availableToTime != null || 
               (availableDays != null && !availableDays.isEmpty());
    }
    
    public boolean hasCertificationFilter() {
        return certifications != null && !certifications.isEmpty();
    }
    
    public boolean hasResponseTimeFilter() {
        return maxResponseHours != null;
    }
    
    public boolean hasWorkingAreaFilter() {
        return workingAreas != null && !workingAreas.isEmpty();
    }
    
    public boolean hasAdvancedRatingFilter() {
        return minReviewCount != null || minCompletionRate != null;
    }
    
    public boolean hasEmergencyFilter() {
        return emergencyService != null && emergencyService;
    }
    
    public boolean isPriceStepDefined() {
        return priceStep != null && priceStep.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isSearchPresetDefined() {
        return searchPreset != null && !searchPreset.trim().isEmpty();
    }
    
    // ==================== PRESET HELPERS ====================
    
    public static EnhancedSearchRequest createEmergencyPreset(Double latitude, Double longitude) {
        EnhancedSearchRequest request = new EnhancedSearchRequest();
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setRadius(5);
        request.setAvailableNow(true);
        request.setEmergencyService(true);
        request.setMaxResponseHours(2);
        request.setMinRating(4.0);
        request.setSortBy("distance");
        request.setSearchPreset("EMERGENCY");
        request.setBoostNearby(true);
        return request;
    }
    
    public static EnhancedSearchRequest createTopRatedPreset() {
        EnhancedSearchRequest request = new EnhancedSearchRequest();
        request.setMinRating(4.5);
        request.setMinReviewCount(10);
        request.setVerifiedTechnician(true);
        request.setSortBy("rating");
        request.setSortDirection("desc");
        request.setSearchPreset("TOP_RATED");
        request.setBoostTopRated(true);
        return request;
    }
    
    public static EnhancedSearchRequest createBudgetFriendlyPreset(BigDecimal maxBudget) {
        EnhancedSearchRequest request = new EnhancedSearchRequest();
        request.setMaxPrice(maxBudget);
        request.setSortBy("price");
        request.setSortDirection("asc");
        request.setSearchPreset("BUDGET_FRIENDLY");
        request.setMinRating(3.5);
        return request;
    }
    
    public static EnhancedSearchRequest createNearbyPreset(Double latitude, Double longitude, Integer radius) {
        EnhancedSearchRequest request = new EnhancedSearchRequest();
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setRadius(radius != null ? radius : 10);
        request.setHasLocation(true);
        request.setSortBy("distance");
        request.setSearchPreset("NEARBY");
        request.setBoostNearby(true);
        return request;
    }
}
