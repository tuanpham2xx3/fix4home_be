package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.SavedSearch;
import com.fix4home.fix4home.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, Long> {
    
    // Find saved searches by user
    List<SavedSearch> findByUserAndIsActiveTrueOrderByCreatedAtDesc(User user);
    Page<SavedSearch> findByUserAndIsActiveTrueOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find all saved searches (including inactive)
    List<SavedSearch> findByUserOrderByCreatedAtDesc(User user);
    
    // Find saved search by name
    Optional<SavedSearch> findByUserAndSearchNameAndIsActiveTrue(User user, String searchName);
    
    // Find saved searches by type
    List<SavedSearch> findByUserAndSearchTypeAndIsActiveTrueOrderByCreatedAtDesc(User user, String searchType);
    
    // Find recently executed saved searches
    @Query("SELECT ss FROM SavedSearch ss WHERE ss.user = :user AND ss.isActive = true AND " +
           "ss.lastExecutedAt >= :since ORDER BY ss.lastExecutedAt DESC")
    List<SavedSearch> findRecentlyExecuted(@Param("user") User user, @Param("since") LocalDateTime since);
    
    // Find most frequently used saved searches
    @Query("SELECT ss FROM SavedSearch ss WHERE ss.user = :user AND ss.isActive = true " +
           "ORDER BY ss.executionCount DESC, ss.lastExecutedAt DESC")
    List<SavedSearch> findMostFrequentlyUsed(User user, Pageable pageable);
    
    // Find saved searches with notifications enabled
    List<SavedSearch> findByUserAndNotificationEnabledTrueAndIsActiveTrue(User user);
    
    // Find all saved searches with notifications enabled (for batch processing)
    List<SavedSearch> findByNotificationEnabledTrueAndIsActiveTrue();
    
    // Count user's saved searches
    Long countByUserAndIsActiveTrue(User user);
    
    // Count saved searches by type
    Long countByUserAndSearchTypeAndIsActiveTrue(User user, String searchType);
    
    // Check if user has reached saved search limit
    @Query("SELECT COUNT(ss) FROM SavedSearch ss WHERE ss.user = :user AND ss.isActive = true")
    Long countActiveSavedSearches(@Param("user") User user);
    
    // Find saved searches that haven't been used recently (for cleanup suggestions)
    @Query("SELECT ss FROM SavedSearch ss WHERE ss.user = :user AND ss.isActive = true AND " +
           "(ss.lastExecutedAt IS NULL OR ss.lastExecutedAt < :cutoffDate) " +
           "ORDER BY ss.createdAt ASC")
    List<SavedSearch> findUnusedSavedSearches(@Param("user") User user, @Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Find popular saved search names (for suggestions)
    @Query("SELECT ss.searchName, COUNT(ss) as count FROM SavedSearch ss " +
           "WHERE ss.isActive = true GROUP BY ss.searchName " +
           "ORDER BY count DESC")
    List<Object[]> findPopularSavedSearchNames(Pageable pageable);
    
    // Search saved searches by name pattern
    @Query("SELECT ss FROM SavedSearch ss WHERE ss.user = :user AND ss.isActive = true AND " +
           "LOWER(ss.searchName) LIKE LOWER(CONCAT('%', :namePattern, '%')) " +
           "ORDER BY ss.executionCount DESC, ss.createdAt DESC")
    List<SavedSearch> findByUserAndSearchNameContainingIgnoreCase(@Param("user") User user, 
                                                                 @Param("namePattern") String namePattern);
    
    // Get saved search statistics for user
    @Query("SELECT ss.searchType, COUNT(ss) as count, SUM(ss.executionCount) as totalExecutions " +
           "FROM SavedSearch ss WHERE ss.user = :user AND ss.isActive = true " +
           "GROUP BY ss.searchType")
    List<Object[]> getUserSavedSearchStatistics(@Param("user") User user);
}
