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
} 