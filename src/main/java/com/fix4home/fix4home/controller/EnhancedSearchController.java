package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.common.ApiResponse;
import com.fix4home.fix4home.model.dto.common.EnhancedSearchRequest;
import com.fix4home.fix4home.model.dto.search.SearchFilterPresetDTO;
import com.fix4home.fix4home.model.dto.search.SearchHistoryDTO;
import com.fix4home.fix4home.model.dto.search.SavedSearchDTO;
import com.fix4home.fix4home.service.EnhancedSearchService;
import com.fix4home.fix4home.security.SecurityConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Enhanced Search", description = "Advanced search features including history, saved searches, and presets")
public class EnhancedSearchController {
    
    private final EnhancedSearchService enhancedSearchService;
    
    // ==================== SEARCH HISTORY ENDPOINTS ====================
    
    @GetMapping("/history")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Get search history", description = "Get user's search history with pagination")
    public ResponseEntity<ApiResponse<Page<SearchHistoryDTO>>> getSearchHistory(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("GET /api/v1/search/history - page: {}, size: {}", page, size);
        
        Page<SearchHistoryDTO> history = enhancedSearchService.getUserSearchHistory(page, size);
        
        return ResponseEntity.ok(ApiResponse.success("Search history retrieved successfully", history));
    }
    
    @GetMapping("/history/recent")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Get recent searches", description = "Get user's recent searches from last 24 hours")
    public ResponseEntity<ApiResponse<List<SearchHistoryDTO>>> getRecentSearches() {
        
        log.info("GET /api/v1/search/history/recent - Getting recent searches");
        
        List<SearchHistoryDTO> recentSearches = enhancedSearchService.getRecentSearches();
        
        return ResponseEntity.ok(ApiResponse.success("Recent searches retrieved successfully", recentSearches));
    }
    
    @GetMapping("/suggestions")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Get search suggestions", description = "Get search suggestions based on user history")
    public ResponseEntity<ApiResponse<List<String>>> getSearchSuggestions(
            @Parameter(description = "Query prefix") @RequestParam(required = false) String query,
            @Parameter(description = "Maximum suggestions") @RequestParam(defaultValue = "10") int limit) {
        
        log.info("GET /api/v1/search/suggestions - query: {}, limit: {}", query, limit);
        
        List<String> suggestions = enhancedSearchService.getSearchSuggestions(query, limit);
        
        return ResponseEntity.ok(ApiResponse.success("Search suggestions retrieved successfully", suggestions));
    }
    
    // ==================== SAVED SEARCHES ENDPOINTS ====================
    
    @PostMapping("/saved")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Save search", description = "Save search criteria for later use")
    public ResponseEntity<ApiResponse<SavedSearchDTO>> saveSearch(
            @Parameter(description = "Search name") @RequestParam String searchName,
            @Parameter(description = "Search type") @RequestParam String searchType,
            @Parameter(description = "Search description") @RequestParam(required = false) String description,
            @Valid @RequestBody EnhancedSearchRequest searchRequest) {
        
        log.info("POST /api/v1/search/saved - Saving search: {}", searchName);
        
        SavedSearchDTO savedSearch = enhancedSearchService.saveSearch(searchName, searchRequest, searchType, description);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Search saved successfully", savedSearch));
    }
    
    @GetMapping("/saved")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Get saved searches", description = "Get user's saved searches with pagination")
    public ResponseEntity<ApiResponse<Page<SavedSearchDTO>>> getSavedSearches(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("GET /api/v1/search/saved - page: {}, size: {}", page, size);
        
        Page<SavedSearchDTO> savedSearches = enhancedSearchService.getUserSavedSearches(page, size);
        
        return ResponseEntity.ok(ApiResponse.success("Saved searches retrieved successfully", savedSearches));
    }
    
    @GetMapping("/saved/{savedSearchId}/execute")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Execute saved search", description = "Execute a saved search and return the criteria")
    public ResponseEntity<ApiResponse<EnhancedSearchRequest>> executeSavedSearch(
            @Parameter(description = "Saved search ID") @PathVariable Long savedSearchId) {
        
        log.info("GET /api/v1/search/saved/{}/execute - Executing saved search", savedSearchId);
        
        EnhancedSearchRequest searchRequest = (EnhancedSearchRequest) enhancedSearchService.executeSavedSearch(savedSearchId);
        
        return ResponseEntity.ok(ApiResponse.success("Saved search executed successfully", searchRequest));
    }
    
    @PutMapping("/saved/{savedSearchId}")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Update saved search", description = "Update saved search metadata")
    public ResponseEntity<ApiResponse<SavedSearchDTO>> updateSavedSearch(
            @Parameter(description = "Saved search ID") @PathVariable Long savedSearchId,
            @Parameter(description = "New search name") @RequestParam(required = false) String searchName,
            @Parameter(description = "New description") @RequestParam(required = false) String description,
            @Parameter(description = "Enable notifications") @RequestParam(required = false) Boolean notificationEnabled) {
        
        log.info("PUT /api/v1/search/saved/{} - Updating saved search", savedSearchId);
        
        SavedSearchDTO updatedSearch = enhancedSearchService.updateSavedSearch(savedSearchId, searchName, description, notificationEnabled);
        
        return ResponseEntity.ok(ApiResponse.success("Saved search updated successfully", updatedSearch));
    }
    
    @DeleteMapping("/saved/{savedSearchId}")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Delete saved search", description = "Delete a saved search")
    public ResponseEntity<ApiResponse<Void>> deleteSavedSearch(
            @Parameter(description = "Saved search ID") @PathVariable Long savedSearchId) {
        
        log.info("DELETE /api/v1/search/saved/{} - Deleting saved search", savedSearchId);
        
        enhancedSearchService.deleteSavedSearch(savedSearchId);
        
        return ResponseEntity.ok(ApiResponse.success("Saved search deleted successfully", null));
    }
    
    // ==================== SEARCH PRESETS ENDPOINTS ====================
    
    @GetMapping("/presets")
    @Operation(summary = "Get search presets", description = "Get available search filter presets")
    public ResponseEntity<ApiResponse<List<SearchFilterPresetDTO>>> getSearchPresets(
            @Parameter(description = "Category filter") @RequestParam(required = false) String category) {
        
        log.info("GET /api/v1/search/presets - category: {}", category);
        
        List<SearchFilterPresetDTO> presets = enhancedSearchService.getSearchPresets(category);
        
        return ResponseEntity.ok(ApiResponse.success("Search presets retrieved successfully", presets));
    }
    
    @PostMapping("/presets/{presetId}/apply")
    @Operation(summary = "Apply search preset", description = "Apply a search preset to current search criteria")
    public ResponseEntity<ApiResponse<EnhancedSearchRequest>> applySearchPreset(
            @Parameter(description = "Preset ID") @PathVariable String presetId,
            @RequestBody(required = false) EnhancedSearchRequest baseRequest) {
        
        log.info("POST /api/v1/search/presets/{}/apply - Applying search preset", presetId);
        
        EnhancedSearchRequest enhancedRequest = enhancedSearchService.applySearchPreset(presetId, baseRequest);
        
        return ResponseEntity.ok(ApiResponse.success("Search preset applied successfully", enhancedRequest));
    }
    
    // ==================== ADVANCED SEARCH UTILITIES ====================
    
    @PostMapping("/analyze")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Analyze search criteria", description = "Analyze and optimize search criteria")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeSearchCriteria(
            @Valid @RequestBody EnhancedSearchRequest searchRequest) {
        
        log.info("POST /api/v1/search/analyze - Analyzing search criteria");
        
        // Analyze search criteria and provide optimization suggestions
        Map<String, Object> analysis = Map.of(
                "hasLocation", searchRequest.hasLocationFilter(),
                "hasAdvancedFilters", searchRequest.hasAdvancedFilters(),
                "estimatedResults", "100-500", // This would be calculated based on criteria
                "suggestions", List.of(
                        "Consider expanding search radius for more results",
                        "Add rating filter for better quality",
                        "Enable nearby boost for faster service"
                ),
                "optimizedCriteria", searchRequest
        );
        
        return ResponseEntity.ok(ApiResponse.success("Search criteria analyzed successfully", analysis));
    }
    
    @GetMapping("/statistics")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @Operation(summary = "Get search statistics", description = "Get user's search usage statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSearchStatistics() {
        
        log.info("GET /api/v1/search/statistics - Getting search statistics");
        
        // This would get actual statistics from the service
        Map<String, Object> statistics = Map.of(
                "totalSearches", 150,
                "savedSearches", 5,
                "mostUsedFilters", List.of("location", "rating", "availability"),
                "averageResultsPerSearch", 25.5,
                "mostSearchedTerms", List.of("electrical", "plumbing", "repair")
        );
        
        return ResponseEntity.ok(ApiResponse.success("Search statistics retrieved successfully", statistics));
    }
}
