package com.fix4home.fix4home.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "saved_searches", indexes = {
    @Index(name = "idx_saved_searches_user_id", columnList = "user_id"),
    @Index(name = "idx_saved_searches_search_type", columnList = "search_type"),
    @Index(name = "idx_saved_searches_is_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedSearch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "search_name", nullable = false, length = 100)
    private String searchName;
    
    @Column(name = "search_type", nullable = false, length = 50)
    private String searchType; // 'TECHNICIAN', 'SERVICE_POST', 'SERVICE'
    
    @Column(name = "search_criteria", columnDefinition = "TEXT", nullable = false)
    private String searchCriteria; // JSON of the complete search criteria
    
    @Column(name = "description", length = 300)
    private String description;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "notification_enabled")
    @Builder.Default
    private Boolean notificationEnabled = false;
    
    @Column(name = "last_executed_at")
    private LocalDateTime lastExecutedAt;
    
    @Column(name = "execution_count")
    @Builder.Default
    private Integer executionCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void incrementExecutionCount() {
        this.executionCount = (this.executionCount != null ? this.executionCount : 0) + 1;
        this.lastExecutedAt = LocalDateTime.now();
    }
    
    public boolean isRecentlyUsed() {
        return lastExecutedAt != null && lastExecutedAt.isAfter(LocalDateTime.now().minusDays(7));
    }
}
