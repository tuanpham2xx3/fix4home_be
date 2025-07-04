package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.Feedback;
import com.fix4home.fix4home.model.entity.ServiceRequest;
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
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    // Basic queries
    Optional<Feedback> findByServiceRequest(ServiceRequest serviceRequest);
    
    @Query("SELECT f FROM Feedback f WHERE f.serviceRequest.id = :serviceRequestId")
    Optional<Feedback> findByServiceRequestId(@Param("serviceRequestId") Long serviceRequestId);
    
    @Query("SELECT f FROM Feedback f WHERE f.customer.id = :customerId")
    Page<Feedback> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);
    
    @Query("SELECT f FROM Feedback f WHERE f.technician.id = :technicianId")
    Page<Feedback> findByTechnicianId(@Param("technicianId") Long technicianId, Pageable pageable);
    
    List<Feedback> findByTechnician(User technician);
    List<Feedback> findByCustomer(User customer);
    
    // Rating queries
    @Query("SELECT f FROM Feedback f WHERE f.rating = :rating")
    Page<Feedback> findByRating(@Param("rating") Integer rating, Pageable pageable);
    
    @Query("SELECT f FROM Feedback f WHERE f.rating >= :minRating")
    Page<Feedback> findByRatingGreaterThanEqual(@Param("minRating") Integer minRating, Pageable pageable);
    
    @Query("SELECT f FROM Feedback f WHERE f.technician.id = :technicianId AND f.rating >= :minRating")
    List<Feedback> findByTechnicianIdAndRatingGreaterThanEqual(@Param("technicianId") Long technicianId, @Param("minRating") Integer minRating);
    
    // Average rating calculation
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.technician.id = :technicianId")
    Double calculateAverageRatingByTechnicianId(@Param("technicianId") Long technicianId);
    
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.technician.id = :technicianId AND f.rating = :rating")
    Long countByTechnicianIdAndRating(@Param("technicianId") Long technicianId, @Param("rating") Integer rating);
    
    // Count queries
    long countByTechnician(User technician);
    long countByCustomer(User customer);
    
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.technician.id = :technicianId")
    long countByTechnicianId(@Param("technicianId") Long technicianId);
    
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.customer.id = :customerId")
    long countByCustomerId(@Param("customerId") Long customerId);
    
    // Reply queries
    @Query("SELECT f FROM Feedback f WHERE f.reply IS NULL")
    Page<Feedback> findByReplyIsNull(Pageable pageable);
    
    @Query("SELECT f FROM Feedback f WHERE f.reply IS NOT NULL")
    Page<Feedback> findByReplyIsNotNull(Pageable pageable);
    
    @Query("SELECT f FROM Feedback f WHERE f.technician.id = :technicianId AND f.reply IS NULL")
    Page<Feedback> findByTechnicianIdAndReplyIsNull(@Param("technicianId") Long technicianId, Pageable pageable);
    
    // Search queries
    @Query("SELECT f FROM Feedback f WHERE LOWER(f.comment) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.reply) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Feedback> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT f FROM Feedback f WHERE f.technician.id = :technicianId AND (LOWER(f.comment) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.reply) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Feedback> searchByTechnicianIdAndKeyword(@Param("technicianId") Long technicianId, @Param("keyword") String keyword, Pageable pageable);
    
    // Date range queries
    @Query("SELECT f FROM Feedback f WHERE f.createdAt BETWEEN :startDate AND :endDate")
    Page<Feedback> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT f FROM Feedback f WHERE f.technician.id = :technicianId AND f.createdAt BETWEEN :startDate AND :endDate")
    List<Feedback> findByTechnicianIdAndCreatedAtBetween(@Param("technicianId") Long technicianId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Recent feedback
    @Query("SELECT f FROM Feedback f ORDER BY f.createdAt DESC")
    Page<Feedback> findRecentFeedback(Pageable pageable);
    
    @Query("SELECT f FROM Feedback f WHERE f.technician.id = :technicianId ORDER BY f.createdAt DESC")
    Page<Feedback> findRecentFeedbackByTechnicianId(@Param("technicianId") Long technicianId, Pageable pageable);
    
    // Top rated technicians
    @Query("SELECT f.technician.id, AVG(f.rating), COUNT(f) FROM Feedback f GROUP BY f.technician.id HAVING COUNT(f) >= :minFeedbackCount ORDER BY AVG(f.rating) DESC")
    List<Object[]> findTopRatedTechnicians(@Param("minFeedbackCount") Long minFeedbackCount);
    
    // Validation queries
    @Query("SELECT COUNT(f) > 0 FROM Feedback f WHERE f.serviceRequest.id = :serviceRequestId")
    boolean existsByServiceRequestId(@Param("serviceRequestId") Long serviceRequestId);
    
    @Query("SELECT COUNT(f) > 0 FROM Feedback f WHERE f.customer.id = :customerId AND f.serviceRequest.id = :serviceRequestId")
    boolean existsByCustomerIdAndServiceRequestId(@Param("customerId") Long customerId, @Param("serviceRequestId") Long serviceRequestId);
} 