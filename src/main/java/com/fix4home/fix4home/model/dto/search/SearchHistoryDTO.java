package com.fix4home.fix4home.model.dto.search;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Search history entry")
public class SearchHistoryDTO {
    
    @Schema(description = "Search history ID")
    private Long id;
    
    @Schema(description = "Type of search", example = "TECHNICIAN")
    private String searchType;
    
    @Schema(description = "Search query/keywords", example = "electrical repair")
    private String searchQuery;
    
    @Schema(description = "Number of results found")
    private Integer resultsCount;
    
    @Schema(description = "Search location", example = "Hanoi, Vietnam")
    private String searchLocation;
    
    @Schema(description = "Filters applied", example = "location,rating,availability")
    private String filtersApplied;
    
    @Schema(description = "Sort criteria", example = "distance")
    private String sortBy;
    
    @Schema(description = "Sort direction", example = "asc")
    private String sortDirection;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Search timestamp")
    private LocalDateTime createdAt;
    
    // Helper methods
    public boolean hasFilters() {
        return filtersApplied != null && !filtersApplied.trim().isEmpty();
    }
    
    public String[] getAppliedFiltersArray() {
        if (!hasFilters()) {
            return new String[0];
        }
        return filtersApplied.split(",");
    }
    
    public boolean isRecentSearch() {
        return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusHours(24));
    }
}
