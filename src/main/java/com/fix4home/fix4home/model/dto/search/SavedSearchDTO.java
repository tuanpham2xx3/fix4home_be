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
@Schema(description = "Saved search information")
public class SavedSearchDTO {
    
    @Schema(description = "Saved search ID")
    private Long id;
    
    @Schema(description = "Search name", example = "Emergency Electricians Near Me")
    private String searchName;
    
    @Schema(description = "Type of search", example = "TECHNICIAN")
    private String searchType;
    
    @Schema(description = "Search description", example = "Emergency electrical services in my area")
    private String description;
    
    @Schema(description = "Whether notifications are enabled for this search")
    private Boolean notificationEnabled;
    
    @Schema(description = "Number of times this search has been executed")
    private Integer executionCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Last execution timestamp")
    private LocalDateTime lastExecutedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Last updated timestamp")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public boolean isRecentlyUsed() {
        return lastExecutedAt != null && lastExecutedAt.isAfter(LocalDateTime.now().minusDays(7));
    }
    
    public boolean isFrequentlyUsed() {
        return executionCount != null && executionCount >= 5;
    }
    
    public boolean hasNotifications() {
        return Boolean.TRUE.equals(notificationEnabled);
    }
}
