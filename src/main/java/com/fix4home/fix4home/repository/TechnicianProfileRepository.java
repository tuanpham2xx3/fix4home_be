package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.TechnicianProfile;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianProfileRepository extends JpaRepository<TechnicianProfile, Long> {
    
    Optional<TechnicianProfile> findByUser(User user);
    
    Optional<TechnicianProfile> findByUserId(Long userId);
    
    List<TechnicianProfile> findByStatus(UserStatus status);
    
    Page<TechnicianProfile> findByStatus(UserStatus status, Pageable pageable);
    
    @Query("SELECT tp FROM TechnicianProfile tp WHERE tp.rating >= :minRating ORDER BY tp.rating DESC")
    List<TechnicianProfile> findByRatingGreaterThanEqualOrderByRatingDesc(@Param("minRating") Float minRating);
    
    @Query("SELECT tp FROM TechnicianProfile tp WHERE tp.fullName LIKE %:name%")
    List<TechnicianProfile> findByFullNameContainingIgnoreCase(@Param("name") String name);
    
    // Online/Offline Status queries
    List<TechnicianProfile> findByIsOnlineAndStatus(Boolean isOnline, UserStatus status);
    
    @Query("SELECT tp FROM TechnicianProfile tp WHERE tp.isOnline = true AND tp.status = :status")
    List<TechnicianProfile> findOnlineTechnicians(@Param("status") UserStatus status);
    
    @Query("SELECT tp FROM TechnicianProfile tp WHERE tp.isOnline = true AND tp.status = :status AND tp.currentLatitude IS NOT NULL AND tp.currentLongitude IS NOT NULL")
    List<TechnicianProfile> findOnlineTechniciansWithLocation(@Param("status") UserStatus status);
    
    // Location-based queries
    @Query("SELECT tp FROM TechnicianProfile tp WHERE tp.currentLatitude IS NOT NULL AND tp.currentLongitude IS NOT NULL AND tp.status = :status")
    List<TechnicianProfile> findTechniciansWithLocation(@Param("status") UserStatus status);
    
    @Query("SELECT tp FROM TechnicianProfile tp WHERE tp.status = :status AND tp.isOnline = :isOnline AND tp.currentLatitude IS NOT NULL AND tp.currentLongitude IS NOT NULL")
    List<TechnicianProfile> findByStatusAndIsOnlineWithLocation(@Param("status") UserStatus status, @Param("isOnline") Boolean isOnline);
    
    // ==================== ADVANCED SEARCH QUERIES ====================
    
    /**
     * Advanced search with text filtering
     */
    @Query("SELECT DISTINCT tp FROM TechnicianProfile tp " +
           "LEFT JOIN tp.user u " +
           "WHERE tp.status = :status " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(tp.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(tp.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<TechnicianProfile> findByKeywordSearch(@Param("keyword") String keyword, 
                                                @Param("status") UserStatus status, 
                                                Pageable pageable);
    
    /**
     * Advanced search with rating filter
     */
    @Query("SELECT tp FROM TechnicianProfile tp " +
           "WHERE tp.status = :status " +
           "AND (:minRating IS NULL OR tp.rating >= :minRating) " +
           "AND (:maxRating IS NULL OR tp.rating <= :maxRating)")
    Page<TechnicianProfile> findByRatingRange(@Param("minRating") Double minRating,
                                              @Param("maxRating") Double maxRating,
                                              @Param("status") UserStatus status,
                                              Pageable pageable);
    
    /**
     * Advanced search with location filter (requires location coordinates)
     */
    @Query("SELECT tp FROM TechnicianProfile tp " +
           "WHERE tp.status = :status " +
           "AND tp.currentLatitude IS NOT NULL " +
           "AND tp.currentLongitude IS NOT NULL " +
           "AND (:availableNow IS NULL OR tp.isOnline = :availableNow)")
    Page<TechnicianProfile> findTechniciansForLocationSearch(@Param("status") UserStatus status,
                                                             @Param("availableNow") Boolean availableNow,
                                                             Pageable pageable);
    
    /**
     * Advanced search with skills filter
     */
    @Query("SELECT DISTINCT tp FROM TechnicianProfile tp " +
           "LEFT JOIN TechnicianSkill ts ON ts.technicianProfile = tp " +
           "WHERE tp.status = :status " +
           "AND (:skillIds IS NULL OR ts.skill.id IN :skillIds)")
    Page<TechnicianProfile> findBySkills(@Param("skillIds") List<Long> skillIds,
                                         @Param("status") UserStatus status,
                                         Pageable pageable);
    
    /**
     * Advanced search with location text filter (address-based)
     */
    @Query("SELECT tp FROM TechnicianProfile tp " +
           "WHERE tp.status = :status " +
           "AND (:location IS NULL OR :location = '' OR " +
           "     LOWER(tp.currentAddress) LIKE LOWER(CONCAT('%', :location, '%')))")
    Page<TechnicianProfile> findByLocationText(@Param("location") String location,
                                               @Param("status") UserStatus status,
                                               Pageable pageable);
    
    /**
     * Comprehensive advanced search with multiple filters
     */
    @Query("SELECT DISTINCT tp FROM TechnicianProfile tp " +
           "LEFT JOIN tp.user u " +
           "LEFT JOIN TechnicianSkill ts ON ts.technicianProfile = tp " +
           "WHERE tp.status = :status " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(tp.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(tp.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:minRating IS NULL OR tp.rating >= :minRating) " +
           "AND (:maxRating IS NULL OR tp.rating <= :maxRating) " +
           "AND (:location IS NULL OR :location = '' OR " +
           "     LOWER(tp.currentAddress) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:availableNow IS NULL OR tp.isOnline = :availableNow) " +
           "AND (:hasLocation IS NULL OR " +
           "     (:hasLocation = true AND tp.currentLatitude IS NOT NULL AND tp.currentLongitude IS NOT NULL) OR " +
           "     (:hasLocation = false)) " +
           "AND (:skillIds IS NULL OR ts.skill.id IN :skillIds)")
    Page<TechnicianProfile> findByAdvancedSearch(@Param("keyword") String keyword,
                                                 @Param("minRating") Double minRating,
                                                 @Param("maxRating") Double maxRating,
                                                 @Param("location") String location,
                                                 @Param("availableNow") Boolean availableNow,
                                                 @Param("hasLocation") Boolean hasLocation,
                                                 @Param("skillIds") List<Long> skillIds,
                                                 @Param("status") UserStatus status,
                                                 Pageable pageable);
    
    /**
     * Count technicians for advanced search (for pagination metadata)
     */
    @Query("SELECT COUNT(DISTINCT tp) FROM TechnicianProfile tp " +
           "LEFT JOIN TechnicianSkill ts ON ts.technicianProfile = tp " +
           "WHERE tp.status = :status " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(tp.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(tp.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:minRating IS NULL OR tp.rating >= :minRating) " +
           "AND (:maxRating IS NULL OR tp.rating <= :maxRating) " +
           "AND (:location IS NULL OR :location = '' OR " +
           "     LOWER(tp.currentAddress) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:availableNow IS NULL OR tp.isOnline = :availableNow) " +
           "AND (:hasLocation IS NULL OR " +
           "     (:hasLocation = true AND tp.currentLatitude IS NOT NULL AND tp.currentLongitude IS NOT NULL) OR " +
           "     (:hasLocation = false)) " +
           "AND (:skillIds IS NULL OR ts.skill.id IN :skillIds)")
    long countByAdvancedSearch(@Param("keyword") String keyword,
                               @Param("minRating") Double minRating,
                               @Param("maxRating") Double maxRating,
                               @Param("location") String location,
                               @Param("availableNow") Boolean availableNow,
                               @Param("hasLocation") Boolean hasLocation,
                               @Param("skillIds") List<Long> skillIds,
                               @Param("status") UserStatus status);
    
    /**
     * Find top rated technicians with minimum review count
     */
    @Query("SELECT tp FROM TechnicianProfile tp " +
           "LEFT JOIN Feedback f ON f.technician = tp.user " +
           "WHERE tp.status = :status " +
           "GROUP BY tp " +
           "HAVING COUNT(f) >= :minReviewCount " +
           "ORDER BY tp.rating DESC")
    Page<TechnicianProfile> findTopRatedTechnicians(@Param("minReviewCount") Long minReviewCount,
                                                    @Param("status") UserStatus status,
                                                    Pageable pageable);
    
    /**
     * Find technicians by experience level
     */
    @Query("SELECT tp FROM TechnicianProfile tp " +
           "WHERE tp.status = :status " +
           "AND (:minExperience IS NULL OR tp.experienceYears >= :minExperience)")
    Page<TechnicianProfile> findByExperienceLevel(@Param("minExperience") Integer minExperience,
                                                   @Param("status") UserStatus status,
                                                   Pageable pageable);
    
    /**
     * Search for available technicians (online and within working radius)
     */
    @Query("SELECT tp FROM TechnicianProfile tp " +
           "WHERE tp.status = :status " +
           "AND tp.isOnline = true " +
           "AND tp.currentLatitude IS NOT NULL " +
           "AND tp.currentLongitude IS NOT NULL " +
           "AND (:workingRadiusFilter IS NULL OR tp.workingRadius >= :workingRadiusFilter)")
    List<TechnicianProfile> findAvailableTechniciansWithLocation(@Param("status") UserStatus status,
                                                                 @Param("workingRadiusFilter") Integer workingRadiusFilter);
} 