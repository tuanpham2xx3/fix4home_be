package com.fix4home.fix4home.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix4home.fix4home.exception.BusinessValidationException;
import com.fix4home.fix4home.exception.ResourceNotFoundException;
import com.fix4home.fix4home.model.dto.common.EnhancedSearchRequest;
import com.fix4home.fix4home.model.dto.common.ServiceSearchRequest;
import com.fix4home.fix4home.model.dto.search.SearchFilterPresetDTO;
import com.fix4home.fix4home.model.dto.search.SearchHistoryDTO;
import com.fix4home.fix4home.model.dto.search.SavedSearchDTO;
import com.fix4home.fix4home.model.entity.SavedSearch;
import com.fix4home.fix4home.model.entity.SearchHistory;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.repository.SavedSearchRepository;
import com.fix4home.fix4home.repository.SearchHistoryRepository;
import com.fix4home.fix4home.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnhancedSearchService extends BaseService {
    
    private final SearchHistoryRepository searchHistoryRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final ObjectMapper objectMapper;
    
    private static final int MAX_SAVED_SEARCHES_PER_USER = 20;
    private static final int MAX_SEARCH_HISTORY_PER_USER = 100;
    
    // ==================== SEARCH HISTORY MANAGEMENT ====================
    
    /**
     * Record a search in history
     */
    public void recordSearch(ServiceSearchRequest searchRequest, String searchType, int resultsCount) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return; // Don't record for anonymous users
        }
        
        // Don't record if user opted out of tracking
        if (searchRequest instanceof EnhancedSearchRequest enhanced && 
            Boolean.FALSE.equals(enhanced.getTrackSearch())) {
            return;
        }
        
        try {
            String searchCriteriaJson = objectMapper.writeValueAsString(searchRequest);
            String filtersApplied = buildFiltersAppliedString(searchRequest);
            
            SearchHistory searchHistory = SearchHistory.builder()
                    .user(currentUser)
                    .searchType(searchType)
                    .searchQuery(searchRequest.getKeyword())
                    .searchCriteria(searchCriteriaJson)
                    .resultsCount(resultsCount)
                    .searchLocation(searchRequest.getLocation())
                    .latitude(searchRequest.getLatitude())
                    .longitude(searchRequest.getLongitude())
                    .radius(searchRequest.getRadius())
                    .filtersApplied(filtersApplied)
                    .sortBy(searchRequest.getSortBy())
                    .sortDirection(searchRequest.getSortDirection())
                    .build();
            
            searchHistoryRepository.save(searchHistory);
            
            // Clean up old history if user has too many entries
            cleanupUserSearchHistory(currentUser);
            
            log.debug("Recorded search history for user: {}, type: {}, results: {}", 
                     currentUser.getId(), searchType, resultsCount);
            
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize search criteria for history", ex);
        }
    }
    
    /**
     * Get user's search history
     */
    @Transactional(readOnly = true)
    public Page<SearchHistoryDTO> getUserSearchHistory(int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        
        Page<SearchHistory> historyPage = searchHistoryRepository.findByUserOrderByCreatedAtDesc(currentUser, pageable);
        
        return historyPage.map(this::convertToSearchHistoryDTO);
    }
    
    /**
     * Get recent searches (last 24 hours)
     */
    @Transactional(readOnly = true)
    public List<SearchHistoryDTO> getRecentSearches() {
        User currentUser = getCurrentUser();
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        
        List<SearchHistory> recentSearches = searchHistoryRepository.findRecentSearches(currentUser, since);
        
        return recentSearches.stream()
                .map(this::convertToSearchHistoryDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get popular search suggestions based on user history
     */
    @Transactional(readOnly = true)
    public List<String> getSearchSuggestions(String query, int limit) {
        User currentUser = getCurrentUser();
        
        // Get user's frequent search queries
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> frequentQueries = searchHistoryRepository.findFrequentSearchQueries(currentUser, 2, pageable);
        
        return frequentQueries.stream()
                .map(row -> (String) row[0])
                .filter(searchQuery -> query == null || searchQuery.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    // ==================== SAVED SEARCHES MANAGEMENT ====================
    
    /**
     * Save a search for later use
     */
    public SavedSearchDTO saveSearch(String searchName, ServiceSearchRequest searchRequest, 
                                   String searchType, String description) {
        User currentUser = getCurrentUser();
        
        // Check if user has reached the limit
        Long existingCount = savedSearchRepository.countActiveSavedSearches(currentUser);
        if (existingCount >= MAX_SAVED_SEARCHES_PER_USER) {
            throw new BusinessValidationException("You have reached the maximum number of saved searches (" + 
                                                 MAX_SAVED_SEARCHES_PER_USER + ")");
        }
        
        // Check if name already exists
        if (savedSearchRepository.findByUserAndSearchNameAndIsActiveTrue(currentUser, searchName).isPresent()) {
            throw new BusinessValidationException("A saved search with this name already exists");
        }
        
        try {
            String searchCriteriaJson = objectMapper.writeValueAsString(searchRequest);
            
            SavedSearch savedSearch = SavedSearch.builder()
                    .user(currentUser)
                    .searchName(searchName)
                    .searchType(searchType)
                    .searchCriteria(searchCriteriaJson)
                    .description(description)
                    .isActive(true)
                    .notificationEnabled(false)
                    .executionCount(0)
                    .build();
            
            savedSearch = savedSearchRepository.save(savedSearch);
            
            log.info("Saved search created: {} for user: {}", searchName, currentUser.getId());
            
            return convertToSavedSearchDTO(savedSearch);
            
        } catch (JsonProcessingException ex) {
            throw new BusinessValidationException("Failed to save search criteria");
        }
    }
    
    /**
     * Execute a saved search
     */
    @Transactional(readOnly = true)
    public ServiceSearchRequest executeSavedSearch(Long savedSearchId) {
        User currentUser = getCurrentUser();
        
        SavedSearch savedSearch = savedSearchRepository.findById(savedSearchId)
                .orElseThrow(() -> ResourceNotFoundException.withMessage("Saved search not found"));
        
        // Check ownership
        if (!savedSearch.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessValidationException("You can only execute your own saved searches");
        }
        
        if (!savedSearch.getIsActive()) {
            throw new BusinessValidationException("This saved search is no longer active");
        }
        
        try {
            // Increment execution count (in separate transaction)
            incrementSavedSearchExecution(savedSearchId);
            
            // Parse and return search criteria
            return objectMapper.readValue(savedSearch.getSearchCriteria(), ServiceSearchRequest.class);
            
        } catch (JsonProcessingException ex) {
            throw new BusinessValidationException("Failed to parse saved search criteria");
        }
    }
    
    /**
     * Get user's saved searches
     */
    @Transactional(readOnly = true)
    public Page<SavedSearchDTO> getUserSavedSearches(int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        
        Page<SavedSearch> savedSearches = savedSearchRepository.findByUserAndIsActiveTrueOrderByCreatedAtDesc(currentUser, pageable);
        
        return savedSearches.map(this::convertToSavedSearchDTO);
    }
    
    /**
     * Update saved search
     */
    public SavedSearchDTO updateSavedSearch(Long savedSearchId, String searchName, String description, 
                                          Boolean notificationEnabled) {
        User currentUser = getCurrentUser();
        
        SavedSearch savedSearch = savedSearchRepository.findById(savedSearchId)
                .orElseThrow(() -> ResourceNotFoundException.withMessage("Saved search not found"));
        
        // Check ownership
        if (!savedSearch.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessValidationException("You can only update your own saved searches");
        }
        
        // Update fields if provided
        if (searchName != null && !searchName.trim().isEmpty()) {
            // Check if new name conflicts with existing
            if (!savedSearch.getSearchName().equals(searchName) &&
                savedSearchRepository.findByUserAndSearchNameAndIsActiveTrue(currentUser, searchName).isPresent()) {
                throw new BusinessValidationException("A saved search with this name already exists");
            }
            savedSearch.setSearchName(searchName);
        }
        
        if (description != null) {
            savedSearch.setDescription(description);
        }
        
        if (notificationEnabled != null) {
            savedSearch.setNotificationEnabled(notificationEnabled);
        }
        
        savedSearch = savedSearchRepository.save(savedSearch);
        
        return convertToSavedSearchDTO(savedSearch);
    }
    
    /**
     * Delete saved search
     */
    public void deleteSavedSearch(Long savedSearchId) {
        User currentUser = getCurrentUser();
        
        SavedSearch savedSearch = savedSearchRepository.findById(savedSearchId)
                .orElseThrow(() -> ResourceNotFoundException.withMessage("Saved search not found"));
        
        // Check ownership
        if (!savedSearch.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessValidationException("You can only delete your own saved searches");
        }
        
        // Soft delete
        savedSearch.setIsActive(false);
        savedSearchRepository.save(savedSearch);
        
        log.info("Deleted saved search: {} for user: {}", savedSearchId, currentUser.getId());
    }
    
    // ==================== SEARCH PRESETS ====================
    
    /**
     * Get available search filter presets
     */
    @Transactional(readOnly = true)
    public List<SearchFilterPresetDTO> getSearchPresets(String category) {
        if (category == null) {
            return SearchFilterPresetDTO.getPopularPresets();
        }
        
        return switch (category.toUpperCase()) {
            case "ELECTRICAL" -> SearchFilterPresetDTO.getElectricalPresets();
            case "PLUMBING" -> SearchFilterPresetDTO.getPlumbingPresets();
            default -> SearchFilterPresetDTO.getPopularPresets();
        };
    }
    
    /**
     * Apply preset to search request
     */
    public EnhancedSearchRequest applySearchPreset(String presetId, EnhancedSearchRequest baseRequest) {
        List<SearchFilterPresetDTO> allPresets = List.of(
                SearchFilterPresetDTO.getPopularPresets(),
                SearchFilterPresetDTO.getElectricalPresets(),
                SearchFilterPresetDTO.getPlumbingPresets()
        ).stream().flatMap(List::stream).toList();
        
        SearchFilterPresetDTO preset = allPresets.stream()
                .filter(p -> p.getPresetId().equals(presetId))
                .findFirst()
                .orElseThrow(() -> new BusinessValidationException("Search preset not found: " + presetId));
        
        // Apply preset criteria to base request
        EnhancedSearchRequest request = new EnhancedSearchRequest();
        
        // Copy existing request fields
        if (baseRequest != null) {
            request.setKeyword(baseRequest.getKeyword());
            request.setLocation(baseRequest.getLocation());
            request.setLatitude(baseRequest.getLatitude());
            request.setLongitude(baseRequest.getLongitude());
        }
        
        // Apply preset criteria
        Map<String, Object> criteria = preset.getFilterCriteria();
        criteria.forEach((key, value) -> {
            switch (key) {
                case "radius" -> request.setRadius((Integer) value);
                case "minRating" -> request.setMinRating((Double) value);
                case "availableNow" -> request.setAvailableNow((Boolean) value);
                case "sortBy" -> request.setSortBy((String) value);
                case "sortDirection" -> request.setSortDirection((String) value);
                // Add more mappings as needed
            }
        });
        
        request.setSearchPreset(presetId);
        
        return request;
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    @Transactional
    private void incrementSavedSearchExecution(Long savedSearchId) {
        SavedSearch savedSearch = savedSearchRepository.findById(savedSearchId).orElse(null);
        if (savedSearch != null) {
            savedSearch.incrementExecutionCount();
            savedSearchRepository.save(savedSearch);
        }
    }
    
    private void cleanupUserSearchHistory(User user) {
        Long count = searchHistoryRepository.countByUser(user);
        if (count > MAX_SEARCH_HISTORY_PER_USER) {
            searchHistoryRepository.deleteExcessUserSearchHistory(user, MAX_SEARCH_HISTORY_PER_USER);
        }
    }
    
    private String buildFiltersAppliedString(ServiceSearchRequest request) {
        List<String> filters = List.of();
        if (request.hasLocationFilter()) filters.add("location");
        if (request.hasPriceFilter()) filters.add("price");
        if (request.hasRatingFilter()) filters.add("rating");
        if (request.hasSkillsFilter()) filters.add("skills");
        if (request.hasServiceFilter()) filters.add("services");
        if (request.getAvailableNow() != null) filters.add("availability");
        
        return String.join(",", filters);
    }
    
    private SearchHistoryDTO convertToSearchHistoryDTO(SearchHistory history) {
        return SearchHistoryDTO.builder()
                .id(history.getId())
                .searchType(history.getSearchType())
                .searchQuery(history.getSearchQuery())
                .resultsCount(history.getResultsCount())
                .searchLocation(history.getSearchLocation())
                .filtersApplied(history.getFiltersApplied())
                .sortBy(history.getSortBy())
                .sortDirection(history.getSortDirection())
                .createdAt(history.getCreatedAt())
                .build();
    }
    
    private SavedSearchDTO convertToSavedSearchDTO(SavedSearch savedSearch) {
        return SavedSearchDTO.builder()
                .id(savedSearch.getId())
                .searchName(savedSearch.getSearchName())
                .searchType(savedSearch.getSearchType())
                .description(savedSearch.getDescription())
                .notificationEnabled(savedSearch.getNotificationEnabled())
                .executionCount(savedSearch.getExecutionCount())
                .lastExecutedAt(savedSearch.getLastExecutedAt())
                .createdAt(savedSearch.getCreatedAt())
                .updatedAt(savedSearch.getUpdatedAt())
                .build();
    }
}
