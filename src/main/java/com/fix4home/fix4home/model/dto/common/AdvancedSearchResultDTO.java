package com.fix4home.fix4home.model.dto.common;

import com.fix4home.fix4home.model.dto.technician.TechnicianSearchResultDTO;
import com.fix4home.fix4home.model.dto.servicepost.ServicePostSearchResultDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedSearchResultDTO {
    
    // Search results
    private List<TechnicianSearchResultDTO> technicians;
    private List<ServicePostSearchResultDTO> servicePosts;
    
    // Pagination metadata
    private PaginationInfo pagination;
    
    // Search metadata
    private SearchMetadata searchInfo;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private int currentPage;
        private int totalPages;
        private long totalElements;
        private int size;
        private boolean hasNext;
        private boolean hasPrevious;
        private boolean isFirst;
        private boolean isLast;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchMetadata {
        private String searchQuery;
        private long searchTimeMs;
        private int filtersApplied;
        private String sortBy;
        private String sortDirection;
        private SearchLocation searchLocation;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchLocation {
        private Double latitude;
        private Double longitude;
        private Integer radius;
        private String locationText;
    }
} 