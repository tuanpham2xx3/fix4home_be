package com.fix4home.fix4home.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_history", indexes = {
    @Index(name = "idx_search_history_user_id", columnList = "user_id"),
    @Index(name = "idx_search_history_created_at", columnList = "created_at"),
    @Index(name = "idx_search_history_search_type", columnList = "search_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "search_type", nullable = false, length = 50)
    private String searchType; // 'TECHNICIAN', 'SERVICE_POST', 'SERVICE'
    
    @Column(name = "search_query", length = 500)
    private String searchQuery; // The actual search keywords
    
    @Column(name = "search_criteria", columnDefinition = "TEXT")
    private String searchCriteria; // JSON of the complete search criteria
    
    @Column(name = "results_count")
    private Integer resultsCount;
    
    @Column(name = "search_location", length = 200)
    private String searchLocation;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "radius")
    private Integer radius;
    
    @Column(name = "filters_applied")
    private String filtersApplied; // Comma-separated list of applied filters
    
    @Column(name = "sort_by", length = 50)
    private String sortBy;
    
    @Column(name = "sort_direction", length = 10)
    private String sortDirection;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Helper methods
    public boolean isRecentSearch() {
        return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusHours(24));
    }
    
    public boolean isLocationBasedSearch() {
        return latitude != null && longitude != null;
    }
}
