package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.SearchHistory;
import com.fix4home.fix4home.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    
    // Find search history by user
    List<SearchHistory> findByUserOrderByCreatedAtDesc(User user);
    Page<SearchHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find recent searches (last 24 hours)
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user = :user AND sh.createdAt >= :since ORDER BY sh.createdAt DESC")
    List<SearchHistory> findRecentSearches(@Param("user") User user, @Param("since") LocalDateTime since);
    
    // Find searches by type
    List<SearchHistory> findByUserAndSearchTypeOrderByCreatedAtDesc(User user, String searchType);
    
    // Find popular search terms
    @Query("SELECT sh.searchQuery, COUNT(sh) as count FROM SearchHistory sh " +
           "WHERE sh.searchQuery IS NOT NULL AND sh.createdAt >= :since " +
           "GROUP BY sh.searchQuery ORDER BY count DESC")
    List<Object[]> findPopularSearchTerms(@Param("since") LocalDateTime since, Pageable pageable);
    
    // Find popular search locations
    @Query("SELECT sh.searchLocation, COUNT(sh) as count FROM SearchHistory sh " +
           "WHERE sh.searchLocation IS NOT NULL AND sh.createdAt >= :since " +
           "GROUP BY sh.searchLocation ORDER BY count DESC")
    List<Object[]> findPopularSearchLocations(@Param("since") LocalDateTime since, Pageable pageable);
    
    // Find searches with similar criteria
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user = :user AND " +
           "sh.searchQuery = :query AND sh.searchType = :type AND " +
           "sh.id != :excludeId ORDER BY sh.createdAt DESC")
    List<SearchHistory> findSimilarSearches(@Param("user") User user, 
                                          @Param("query") String query, 
                                          @Param("type") String type, 
                                          @Param("excludeId") Long excludeId, 
                                          Pageable pageable);
    
    // Find searches in location radius
    @Query("SELECT sh FROM SearchHistory sh WHERE " +
           "sh.latitude IS NOT NULL AND sh.longitude IS NOT NULL AND " +
           "sh.createdAt >= :since AND " +
           "(6371 * acos(cos(radians(:lat)) * cos(radians(sh.latitude)) * " +
           "cos(radians(sh.longitude) - radians(:lng)) + " +
           "sin(radians(:lat)) * sin(radians(sh.latitude)))) <= :radiusKm " +
           "ORDER BY sh.createdAt DESC")
    List<SearchHistory> findSearchesInRadius(@Param("lat") Double latitude,
                                           @Param("lng") Double longitude,
                                           @Param("radiusKm") Double radiusKm,
                                           @Param("since") LocalDateTime since);
    
    // Count user searches
    Long countByUser(User user);
    
    // Count searches by type for user
    Long countByUserAndSearchType(User user, String searchType);
    
    // Count searches in time period
    @Query("SELECT COUNT(sh) FROM SearchHistory sh WHERE sh.user = :user AND sh.createdAt >= :since")
    Long countUserSearchesSince(@Param("user") User user, @Param("since") LocalDateTime since);
    
    // Delete old search history
    @Modifying
    @Query("DELETE FROM SearchHistory sh WHERE sh.createdAt < :cutoffDate")
    int deleteOldSearchHistory(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Delete user search history beyond limit
    @Modifying
    @Query("DELETE FROM SearchHistory sh WHERE sh.user = :user AND sh.id NOT IN " +
           "(SELECT sh2.id FROM SearchHistory sh2 WHERE sh2.user = :user ORDER BY sh2.createdAt DESC LIMIT :keepCount)")
    int deleteExcessUserSearchHistory(@Param("user") User user, @Param("keepCount") int keepCount);
    
    // Get search statistics
    @Query("SELECT sh.searchType, COUNT(sh) as count, AVG(sh.resultsCount) as avgResults " +
           "FROM SearchHistory sh WHERE sh.user = :user AND sh.createdAt >= :since " +
           "GROUP BY sh.searchType")
    List<Object[]> getUserSearchStatistics(@Param("user") User user, @Param("since") LocalDateTime since);
    
    // Find frequently searched queries by user
    @Query("SELECT sh.searchQuery, COUNT(sh) as count FROM SearchHistory sh " +
           "WHERE sh.user = :user AND sh.searchQuery IS NOT NULL " +
           "GROUP BY sh.searchQuery HAVING COUNT(sh) >= :minCount " +
           "ORDER BY count DESC")
    List<Object[]> findFrequentSearchQueries(@Param("user") User user, 
                                           @Param("minCount") int minCount, 
                                           Pageable pageable);
}
