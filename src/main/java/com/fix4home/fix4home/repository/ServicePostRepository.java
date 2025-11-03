package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.ServicePost;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.ServicePostStatus;
import com.fix4home.fix4home.model.enums.ServicePostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServicePostRepository extends JpaRepository<ServicePost, Long> {
    
    // Find posts by customer
    List<ServicePost> findByCustomerOrderByCreatedAtDesc(User customer);
    
    Page<ServicePost> findByCustomerOrderByCreatedAtDesc(User customer, Pageable pageable);
    
    // Find active posts for technicians to view
    @Query("SELECT sp FROM ServicePost sp WHERE sp.status IN :statuses " +
           "AND (sp.expiresAt IS NULL OR sp.expiresAt > :now) " +
           "ORDER BY sp.createdAt DESC")
    List<ServicePost> findActivePostsForTechnicians(@Param("statuses") List<ServicePostStatus> statuses, 
                                                   @Param("now") LocalDateTime now);
    
    @Query("SELECT sp FROM ServicePost sp WHERE sp.status IN :statuses " +
           "AND (sp.expiresAt IS NULL OR sp.expiresAt > :now) " +
           "ORDER BY sp.createdAt DESC")
    Page<ServicePost> findActivePostsForTechnicians(@Param("statuses") List<ServicePostStatus> statuses, 
                                                   @Param("now") LocalDateTime now, 
                                                   Pageable pageable);
    
    // Find posts by service
    List<ServicePost> findByServiceIdAndStatusInOrderByCreatedAtDesc(Long serviceId, List<ServicePostStatus> statuses);
    
    // Find posts by type
    List<ServicePost> findByTypeAndStatusInOrderByCreatedAtDesc(ServicePostType type, List<ServicePostStatus> statuses);
    
    // Find posts by status
    List<ServicePost> findByStatusOrderByCreatedAtDesc(ServicePostStatus status);
    
    // Find expired posts
    @Query("SELECT sp FROM ServicePost sp WHERE sp.expiresAt IS NOT NULL AND sp.expiresAt < :now " +
           "AND sp.status NOT IN ('COMPLETED', 'CANCELLED', 'EXPIRED')")
    List<ServicePost> findExpiredPosts(@Param("now") LocalDateTime now);
    
    // Search posts by title or description
    @Query("SELECT sp FROM ServicePost sp WHERE " +
           "(LOWER(sp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sp.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND sp.status IN :statuses " +
           "ORDER BY sp.createdAt DESC")
    List<ServicePost> searchPosts(@Param("keyword") String keyword, @Param("statuses") List<ServicePostStatus> statuses);
    
    // Find posts by location (address)
    @Query("SELECT sp FROM ServicePost sp WHERE " +
           "LOWER(sp.address.address) LIKE LOWER(CONCAT('%', :location, '%')) " +
           "AND sp.status IN :statuses " +
           "ORDER BY sp.createdAt DESC")
    List<ServicePost> findPostsByLocation(@Param("location") String location, @Param("statuses") List<ServicePostStatus> statuses);
    
    // Statistics queries
    @Query("SELECT COUNT(sp) FROM ServicePost sp WHERE sp.customer = :customer")
    Long countByCustomer(@Param("customer") User customer);
    
    @Query("SELECT COUNT(sp) FROM ServicePost sp WHERE sp.status = :status")
    Long countByStatus(@Param("status") ServicePostStatus status);
    
    @Query("SELECT COUNT(sp) FROM ServicePost sp WHERE sp.createdAt >= :fromDate")
    Long countCreatedAfter(@Param("fromDate") LocalDateTime fromDate);
    
    // Find posts that technician hasn't responded to yet
    @Query("SELECT sp FROM ServicePost sp WHERE sp.status IN :statuses " +
           "AND (sp.expiresAt IS NULL OR sp.expiresAt > :now) " +
           "AND sp.id NOT IN (" +
           "    SELECT spr.servicePost.id FROM ServicePostResponse spr " +
           "    WHERE spr.technician = :technician" +
           ") " +
           "ORDER BY sp.createdAt DESC")
    List<ServicePost> findPostsNotRespondedByTechnician(@Param("technician") User technician, 
                                                        @Param("statuses") List<ServicePostStatus> statuses,
                                                        @Param("now") LocalDateTime now);
    
    // Find urgent posts
    @Query("SELECT sp FROM ServicePost sp WHERE sp.type = 'URGENT' " +
           "AND sp.status IN :statuses " +
           "AND (sp.expiresAt IS NULL OR sp.expiresAt > :now) " +
           "ORDER BY sp.createdAt DESC")
    List<ServicePost> findUrgentPosts(@Param("statuses") List<ServicePostStatus> statuses, 
                                     @Param("now") LocalDateTime now);
    
    // ==================== ADVANCED SEARCH QUERIES ====================
    
    /**
     * Advanced search with budget range filter
     */
    @Query("SELECT sp FROM ServicePost sp WHERE sp.status IN :statuses " +
           "AND (sp.expiresAt IS NULL OR sp.expiresAt > :now) " +
           "AND (:minBudget IS NULL OR sp.estimatedBudget >= :minBudget) " +
           "AND (:maxBudget IS NULL OR sp.estimatedBudget <= :maxBudget) " +
           "ORDER BY sp.createdAt DESC")
    Page<ServicePost> findByBudgetRange(@Param("minBudget") BigDecimal minBudget,
                                        @Param("maxBudget") BigDecimal maxBudget,
                                        @Param("statuses") List<ServicePostStatus> statuses,
                                        @Param("now") LocalDateTime now,
                                        Pageable pageable);
    
    /**
     * Advanced search with keyword and multiple filters
     */
    @Query("SELECT DISTINCT sp FROM ServicePost sp " +
           "LEFT JOIN sp.service s " +
           "WHERE sp.status IN :statuses " +
           "AND (sp.expiresAt IS NULL OR sp.expiresAt > :now) " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(sp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(sp.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:location IS NULL OR :location = '' OR " +
           "     LOWER(sp.address.address) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:type IS NULL OR sp.type = :type) " +
           "AND (:minBudget IS NULL OR sp.estimatedBudget >= :minBudget) " +
           "AND (:maxBudget IS NULL OR sp.estimatedBudget <= :maxBudget) " +
           "AND (:serviceIds IS NULL OR s.id IN :serviceIds) " +
           "ORDER BY " +
           "CASE WHEN sp.type = 'URGENT' THEN 0 ELSE 1 END, " +
           "sp.createdAt DESC")
    Page<ServicePost> findByAdvancedSearch(@Param("keyword") String keyword,
                                           @Param("location") String location,
                                           @Param("type") ServicePostType type,
                                           @Param("minBudget") BigDecimal minBudget,
                                           @Param("maxBudget") BigDecimal maxBudget,
                                           @Param("serviceIds") List<Long> serviceIds,
                                           @Param("statuses") List<ServicePostStatus> statuses,
                                           @Param("now") LocalDateTime now,
                                           Pageable pageable);
    
    /**
     * Count service posts for advanced search (for pagination metadata)
     */
    @Query("SELECT COUNT(DISTINCT sp) FROM ServicePost sp " +
           "LEFT JOIN sp.service s " +
           "WHERE sp.status IN :statuses " +
           "AND (sp.expiresAt IS NULL OR sp.expiresAt > :now) " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(sp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(sp.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:location IS NULL OR :location = '' OR " +
           "     LOWER(sp.address.address) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:type IS NULL OR sp.type = :type) " +
           "AND (:minBudget IS NULL OR sp.estimatedBudget >= :minBudget) " +
           "AND (:maxBudget IS NULL OR sp.estimatedBudget <= :maxBudget) " +
           "AND (:serviceIds IS NULL OR s.id IN :serviceIds)")
    long countByAdvancedSearch(@Param("keyword") String keyword,
                               @Param("location") String location,
                               @Param("type") ServicePostType type,
                               @Param("minBudget") BigDecimal minBudget,
                               @Param("maxBudget") BigDecimal maxBudget,
                               @Param("serviceIds") List<Long> serviceIds,
                               @Param("statuses") List<ServicePostStatus> statuses,
                               @Param("now") LocalDateTime now);
    
    /**
     * Find service posts with location coordinates for distance calculation
     */
    @Query("SELECT sp FROM ServicePost sp WHERE sp.status IN :statuses " +
           "AND (sp.expiresAt IS NULL OR sp.expiresAt > :now) " +
           "AND sp.address.latitude IS NOT NULL " +
           "AND sp.address.longitude IS NOT NULL " +
           "ORDER BY sp.createdAt DESC")
    List<ServicePost> findPostsWithLocationCoordinates(@Param("statuses") List<ServicePostStatus> statuses,
                                                       @Param("now") LocalDateTime now);
    
    /**
     * Find expiring soon posts (within 24 hours)
     */
    @Query("SELECT sp FROM ServicePost sp WHERE sp.status IN :statuses " +
           "AND sp.expiresAt IS NOT NULL " +
           "AND sp.expiresAt BETWEEN :now AND :expiringSoonLimit " +
           "ORDER BY sp.expiresAt ASC")
    List<ServicePost> findExpiringSoonPosts(@Param("statuses") List<ServicePostStatus> statuses,
                                           @Param("now") LocalDateTime now,
                                           @Param("expiringSoonLimit") LocalDateTime expiringSoonLimit);
    
    /**
     * Find posts by service category with filters
     */
    @Query("SELECT sp FROM ServicePost sp " +
           "LEFT JOIN sp.service s " +
           "WHERE sp.status IN :statuses " +
           "AND (sp.expiresAt IS NULL OR sp.expiresAt > :now) " +
           "AND (:serviceIds IS NULL OR s.id IN :serviceIds) " +
           "AND (:type IS NULL OR sp.type = :type) " +
           "ORDER BY " +
           "CASE WHEN sp.type = 'URGENT' THEN 0 ELSE 1 END, " +
           "sp.createdAt DESC")
    Page<ServicePost> findByServiceCategoryWithFilters(@Param("serviceIds") List<Long> serviceIds,
                                                       @Param("type") ServicePostType type,
                                                       @Param("statuses") List<ServicePostStatus> statuses,
                                                       @Param("now") LocalDateTime now,
                                                       Pageable pageable);
    
    /**
     * Find posts by preferred time range
     */
    @Query("SELECT sp FROM ServicePost sp WHERE sp.status IN :statuses " +
           "AND (sp.expiresAt IS NULL OR sp.expiresAt > :now) " +
           "AND (:fromTime IS NULL OR sp.preferredTime >= :fromTime) " +
           "AND (:toTime IS NULL OR sp.preferredTime <= :toTime) " +
           "ORDER BY sp.preferredTime ASC")
    Page<ServicePost> findByPreferredTimeRange(@Param("fromTime") LocalDateTime fromTime,
                                               @Param("toTime") LocalDateTime toTime,
                                               @Param("statuses") List<ServicePostStatus> statuses,
                                               @Param("now") LocalDateTime now,
                                               Pageable pageable);
    
    /**
     * Find high-value posts (above specified budget threshold)
     */
    @Query("SELECT sp FROM ServicePost sp WHERE sp.status IN :statuses " +
           "AND (sp.expiresAt IS NULL OR sp.expiresAt > :now) " +
           "AND sp.estimatedBudget >= :minBudget " +
           "ORDER BY sp.estimatedBudget DESC")
    Page<ServicePost> findHighValuePosts(@Param("minBudget") BigDecimal minBudget,
                                         @Param("statuses") List<ServicePostStatus> statuses,
                                         @Param("now") LocalDateTime now,
                                         Pageable pageable);
    
    /**
     * Find posts excluding those already responded to by technician
     */
    @Query("SELECT sp FROM ServicePost sp WHERE sp.status IN :statuses " +
           "AND (sp.expiresAt IS NULL OR sp.expiresAt > :now) " +
           "AND (:excludeTechnicianId IS NULL OR sp.id NOT IN (" +
           "    SELECT spr.servicePost.id FROM ServicePostResponse spr " +
           "    WHERE spr.technician.id = :excludeTechnicianId" +
           ")) " +
           "ORDER BY " +
           "CASE WHEN sp.type = 'URGENT' THEN 0 ELSE 1 END, " +
           "sp.createdAt DESC")
    Page<ServicePost> findAvailablePostsForTechnician(@Param("excludeTechnicianId") Long excludeTechnicianId,
                                                      @Param("statuses") List<ServicePostStatus> statuses,
                                                      @Param("now") LocalDateTime now,
                                                      Pageable pageable);
} 