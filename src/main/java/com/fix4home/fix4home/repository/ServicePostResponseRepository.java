package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.ServicePost;
import com.fix4home.fix4home.model.entity.ServicePostResponse;
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
public interface ServicePostResponseRepository extends JpaRepository<ServicePostResponse, Long> {
    
    // Find responses by service post
    List<ServicePostResponse> findByServicePostOrderByCreatedAtDesc(ServicePost servicePost);
    
    Page<ServicePostResponse> findByServicePostOrderByCreatedAtDesc(ServicePost servicePost, Pageable pageable);
    
    // Find responses by technician
    List<ServicePostResponse> findByTechnicianOrderByCreatedAtDesc(User technician);
    
    Page<ServicePostResponse> findByTechnicianOrderByCreatedAtDesc(User technician, Pageable pageable);
    
    // Check if technician already responded to a post
    boolean existsByServicePostAndTechnician(ServicePost servicePost, User technician);
    
    // Find specific response by service post and technician
    Optional<ServicePostResponse> findByServicePostAndTechnician(ServicePost servicePost, User technician);
    
    // Find selected responses
    List<ServicePostResponse> findByIsSelectedTrueOrderByCreatedAtDesc();
    
    // Find responses by service post and selection status
    List<ServicePostResponse> findByServicePostAndIsSelectedOrderByCreatedAtDesc(ServicePost servicePost, Boolean isSelected);
    
    // Count responses for a service post
    @Query("SELECT COUNT(spr) FROM ServicePostResponse spr WHERE spr.servicePost = :servicePost")
    Long countByServicePost(@Param("servicePost") ServicePost servicePost);
    
    // Count responses by technician
    @Query("SELECT COUNT(spr) FROM ServicePostResponse spr WHERE spr.technician = :technician")
    Long countByTechnician(@Param("technician") User technician);
    
    // Count selected responses by technician
    @Query("SELECT COUNT(spr) FROM ServicePostResponse spr WHERE spr.technician = :technician AND spr.isSelected = true")
    Long countSelectedByTechnician(@Param("technician") User technician);
    
    // Find responses created after a date
    @Query("SELECT spr FROM ServicePostResponse spr WHERE spr.createdAt >= :fromDate ORDER BY spr.createdAt DESC")
    List<ServicePostResponse> findResponsesCreatedAfter(@Param("fromDate") LocalDateTime fromDate);
    
    // Find top technicians by response count
    @Query("SELECT spr.technician, COUNT(spr) as responseCount FROM ServicePostResponse spr " +
           "WHERE spr.createdAt >= :fromDate " +
           "GROUP BY spr.technician " +
           "ORDER BY responseCount DESC")
    List<Object[]> findTopTechniciansByResponseCount(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);
    
    // Find top technicians by selection rate
    @Query("SELECT spr.technician, " +
           "COUNT(spr) as totalResponses, " +
           "SUM(CASE WHEN spr.isSelected = true THEN 1 ELSE 0 END) as selectedResponses " +
           "FROM ServicePostResponse spr " +
           "WHERE spr.createdAt >= :fromDate " +
           "GROUP BY spr.technician " +
           "HAVING COUNT(spr) >= :minResponses " +
           "ORDER BY (SUM(CASE WHEN spr.isSelected = true THEN 1 ELSE 0 END) * 1.0 / COUNT(spr)) DESC")
    List<Object[]> findTopTechniciansBySelectionRate(@Param("fromDate") LocalDateTime fromDate, 
                                                    @Param("minResponses") Long minResponses, 
                                                    Pageable pageable);
    
    // Delete responses by service post (for cleanup)
    void deleteByServicePost(ServicePost servicePost);
    
    // Find responses with quoted price in range
    @Query("SELECT spr FROM ServicePostResponse spr WHERE " +
           "spr.quotedPrice >= :minPrice AND spr.quotedPrice <= :maxPrice " +
           "ORDER BY spr.quotedPrice ASC")
    List<ServicePostResponse> findByQuotedPriceRange(@Param("minPrice") java.math.BigDecimal minPrice, 
                                                    @Param("maxPrice") java.math.BigDecimal maxPrice);
    
    // Find average quoted price for a service post
    @Query("SELECT AVG(spr.quotedPrice) FROM ServicePostResponse spr WHERE spr.servicePost = :servicePost")
    java.math.BigDecimal findAverageQuotedPriceByServicePost(@Param("servicePost") ServicePost servicePost);
} 