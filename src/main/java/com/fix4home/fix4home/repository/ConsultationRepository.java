package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.Consultation;
import com.fix4home.fix4home.model.entity.ServicePost;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.ConsultationStatus;
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
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    
    // Find consultations by technician
    List<Consultation> findByTechnicianOrderBySubmittedAtDesc(User technician);
    
    Page<Consultation> findByTechnicianOrderBySubmittedAtDesc(User technician, Pageable pageable);
    
    // Find consultations by service post
    List<Consultation> findByServicePostOrderBySubmittedAtDesc(ServicePost servicePost);
    
    Page<Consultation> findByServicePostOrderBySubmittedAtDesc(ServicePost servicePost, Pageable pageable);
    
    // Find consultations by service post and status
    List<Consultation> findByServicePostAndStatusOrderBySubmittedAtDesc(ServicePost servicePost, ConsultationStatus status);
    
    // Find consultations by technician and status
    List<Consultation> findByTechnicianAndStatusOrderBySubmittedAtDesc(User technician, ConsultationStatus status);
    
    // Check if technician already submitted consultation for a service post
    Optional<Consultation> findByServicePostAndTechnician(ServicePost servicePost, User technician);
    
    // Find pending consultations for a service post
    @Query("SELECT c FROM Consultation c WHERE c.servicePost = :servicePost " +
           "AND c.status = 'PENDING' ORDER BY c.submittedAt ASC")
    List<Consultation> findPendingConsultationsByServicePost(@Param("servicePost") ServicePost servicePost);
    
    // Find accepted consultations for a service post
    @Query("SELECT c FROM Consultation c WHERE c.servicePost = :servicePost " +
           "AND c.status = 'ACCEPTED' ORDER BY c.respondedAt DESC")
    List<Consultation> findAcceptedConsultationsByServicePost(@Param("servicePost") ServicePost servicePost);
    
    // Find consultations by customer (through service post)
    @Query("SELECT c FROM Consultation c WHERE c.servicePost.customer = :customer " +
           "ORDER BY c.submittedAt DESC")
    List<Consultation> findConsultationsByCustomer(@Param("customer") User customer);
    
    @Query("SELECT c FROM Consultation c WHERE c.servicePost.customer = :customer " +
           "ORDER BY c.submittedAt DESC")
    Page<Consultation> findConsultationsByCustomer(@Param("customer") User customer, Pageable pageable);
    
    // Find consultations by status
    List<Consultation> findByStatusOrderBySubmittedAtDesc(ConsultationStatus status);
    
    // Count consultations by technician
    @Query("SELECT COUNT(c) FROM Consultation c WHERE c.technician = :technician")
    Long countByTechnician(@Param("technician") User technician);
    
    // Count consultations by status for technician
    @Query("SELECT COUNT(c) FROM Consultation c WHERE c.technician = :technician AND c.status = :status")
    Long countByTechnicianAndStatus(@Param("technician") User technician, @Param("status") ConsultationStatus status);
    
    // Count consultations by service post
    @Query("SELECT COUNT(c) FROM Consultation c WHERE c.servicePost = :servicePost")
    Long countByServicePost(@Param("servicePost") ServicePost servicePost);
    
    // Count consultations by service post and status
    @Query("SELECT COUNT(c) FROM Consultation c WHERE c.servicePost = :servicePost AND c.status = :status")
    Long countByServicePostAndStatus(@Param("servicePost") ServicePost servicePost, @Param("status") ConsultationStatus status);
    
    // Find consultations submitted after a certain date
    @Query("SELECT c FROM Consultation c WHERE c.submittedAt >= :fromDate ORDER BY c.submittedAt DESC")
    List<Consultation> findConsultationsSubmittedAfter(@Param("fromDate") LocalDateTime fromDate);
    
    // Find top-rated technicians based on accepted consultations
    @Query("SELECT c.technician, COUNT(c) as acceptedCount FROM Consultation c " +
           "WHERE c.status = 'ACCEPTED' GROUP BY c.technician ORDER BY acceptedCount DESC")
    List<Object[]> findTopTechniciansByAcceptedConsultations();
    
    // Find consultations with quoted price in range
    @Query("SELECT c FROM Consultation c WHERE c.quotedPrice BETWEEN :minPrice AND :maxPrice " +
           "ORDER BY c.quotedPrice ASC")
    List<Consultation> findConsultationsByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice, 
                                                     @Param("maxPrice") java.math.BigDecimal maxPrice);
    
    // Delete consultations by service post (for cleanup)
    void deleteByServicePost(ServicePost servicePost);
} 