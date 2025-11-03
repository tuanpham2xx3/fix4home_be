package com.fix4home.fix4home.model.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilterPresetDTO {
    
    private String presetId;
    private String name;
    private String description;
    private String icon;
    private String color;
    private Boolean isDefault;
    private Integer usageCount;
    
    // Filter criteria
    private Map<String, Object> filterCriteria;
    
    // Predefined popular presets
    public static List<SearchFilterPresetDTO> getPopularPresets() {
        return List.of(
                // Emergency Services
                SearchFilterPresetDTO.builder()
                        .presetId("EMERGENCY")
                        .name("Emergency Services")
                        .description("Fast response technicians available now")
                        .icon("🚨")
                        .color("#FF4444")
                        .filterCriteria(Map.of(
                                "availableNow", true,
                                "maxResponseHours", 2,
                                "emergencyService", true,
                                "minRating", 4.0,
                                "radius", 5,
                                "sortBy", "distance"
                        ))
                        .build(),
                
                // Top Rated
                SearchFilterPresetDTO.builder()
                        .presetId("TOP_RATED")
                        .name("Top Rated Experts")
                        .description("Highest rated and verified technicians")
                        .icon("⭐")
                        .color("#FFD700")
                        .filterCriteria(Map.of(
                                "minRating", 4.5,
                                "minReviewCount", 10,
                                "verifiedTechnician", true,
                                "sortBy", "rating",
                                "sortDirection", "desc"
                        ))
                        .build(),
                
                // Budget Friendly
                SearchFilterPresetDTO.builder()
                        .presetId("BUDGET_FRIENDLY")
                        .name("Budget Friendly")
                        .description("Quality service at affordable prices")
                        .icon("💰")
                        .color("#4CAF50")
                        .filterCriteria(Map.of(
                                "sortBy", "price",
                                "sortDirection", "asc",
                                "minRating", 3.5,
                                "maxPrice", new BigDecimal("500000")
                        ))
                        .build(),
                
                // Nearby Technicians
                SearchFilterPresetDTO.builder()
                        .presetId("NEARBY")
                        .name("Nearby Technicians")
                        .description("Technicians in your area")
                        .icon("📍")
                        .color("#2196F3")
                        .filterCriteria(Map.of(
                                "radius", 10,
                                "hasLocation", true,
                                "sortBy", "distance"
                        ))
                        .build(),
                
                // Experienced Professionals
                SearchFilterPresetDTO.builder()
                        .presetId("EXPERIENCED")
                        .name("Experienced Professionals")
                        .description("Technicians with 5+ years experience")
                        .icon("🎯")
                        .color("#9C27B0")
                        .filterCriteria(Map.of(
                                "minExperienceYears", 5,
                                "minRating", 4.0,
                                "sortBy", "experience",
                                "sortDirection", "desc"
                        ))
                        .build(),
                
                // Certified Specialists
                SearchFilterPresetDTO.builder()
                        .presetId("CERTIFIED")
                        .name("Certified Specialists")
                        .description("Certified and insured technicians")
                        .icon("🏆")
                        .color("#FF9800")
                        .filterCriteria(Map.of(
                                "verifiedTechnician", true,
                                "insuranceCovered", true,
                                "warrantyOffered", true,
                                "sortBy", "rating",
                                "sortDirection", "desc"
                        ))
                        .build(),
                
                // Quick Response
                SearchFilterPresetDTO.builder()
                        .presetId("QUICK_RESPONSE")
                        .name("Quick Response")
                        .description("Fast responding technicians")
                        .icon("⚡")
                        .color("#FF5722")
                        .filterCriteria(Map.of(
                                "maxResponseHours", 4,
                                "availableNow", true,
                                "sortBy", "distance"
                        ))
                        .build(),
                
                // Weekend Available
                SearchFilterPresetDTO.builder()
                        .presetId("WEEKEND_AVAILABLE")
                        .name("Weekend Available")
                        .description("Technicians available on weekends")
                        .icon("📅")
                        .color("#607D8B")
                        .filterCriteria(Map.of(
                                "availableDays", List.of("SAT", "SUN"),
                                "sortBy", "rating",
                                "sortDirection", "desc"
                        ))
                        .build()
        );
    }
    
    // Category-specific presets
    public static List<SearchFilterPresetDTO> getElectricalPresets() {
        return List.of(
                SearchFilterPresetDTO.builder()
                        .presetId("ELECTRICAL_EMERGENCY")
                        .name("Electrical Emergency")
                        .description("Emergency electrical services")
                        .icon("⚡")
                        .color("#FF4444")
                        .filterCriteria(Map.of(
                                "serviceCategories", List.of("ELECTRICAL"),
                                "emergencyService", true,
                                "maxResponseHours", 1,
                                "radius", 5
                        ))
                        .build(),
                
                SearchFilterPresetDTO.builder()
                        .presetId("ELECTRICAL_INSTALLATION")
                        .name("Electrical Installation")
                        .description("Professional electrical installation")
                        .icon("🔌")
                        .color("#FFC107")
                        .filterCriteria(Map.of(
                                "serviceCategories", List.of("ELECTRICAL"),
                                "minExperienceYears", 3,
                                "verifiedTechnician", true,
                                "warrantyOffered", true
                        ))
                        .build()
        );
    }
    
    public static List<SearchFilterPresetDTO> getPlumbingPresets() {
        return List.of(
                SearchFilterPresetDTO.builder()
                        .presetId("PLUMBING_EMERGENCY")
                        .name("Plumbing Emergency")
                        .description("Emergency plumbing services")
                        .icon("🚿")
                        .color("#FF4444")
                        .filterCriteria(Map.of(
                                "serviceCategories", List.of("PLUMBING"),
                                "emergencyService", true,
                                "maxResponseHours", 2,
                                "radius", 10
                        ))
                        .build(),
                
                SearchFilterPresetDTO.builder()
                        .presetId("PLUMBING_MAINTENANCE")
                        .name("Plumbing Maintenance")
                        .description("Regular plumbing maintenance")
                        .icon("🔧")
                        .color("#2196F3")
                        .filterCriteria(Map.of(
                                "serviceCategories", List.of("PLUMBING"),
                                "minRating", 4.0,
                                "sortBy", "price"
                        ))
                        .build()
        );
    }
}
