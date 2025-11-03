package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.Complaint;
import com.fix4home.fix4home.model.entity.ServiceRequest;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.ComplaintStatus;
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
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    
    // Basic query methods
    List<Complaint> findByComplainant(User complainant);
    
    List<Complaint> findByAccused(User accused);
    
    List<Complaint> findByStatus(ComplaintStatus status);
    
    List<Complaint> findByServiceRequest(ServiceRequest serviceRequest);
    
    // Combined query methods  
    List<Complaint> findByComplainantAndStatus(User complainant, ComplaintStatus status);
    
    List<Complaint> findByAccusedAndStatus(User accused, ComplaintStatus status);
    
    // Check if complaint already exists for a service request
    @Query("SELECT c FROM Complaint c WHERE c.serviceRequest = :serviceRequest AND c.complainant = :complainant")
    Optional<Complaint> findByServiceRequestAndComplainant(@Param("serviceRequest") ServiceRequest serviceRequest, 
                                                          @Param("complainant") User complainant);
    
    // Admin queries
    @Query("SELECT c FROM Complaint c WHERE c.status = :status ORDER BY c.createdAt ASC")
    List<Complaint> findPendingComplaints(@Param("status") ComplaintStatus status);
    
    @Query("SELECT c FROM Complaint c WHERE c.resolvedBy = :admin ORDER BY c.resolvedAt DESC")
    List<Complaint> findByResolvedBy(@Param("admin") User admin);
    
    // Enhanced queries with pagination
    Page<Complaint> findByComplainant(User complainant, Pageable pageable);
    
    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);
    
    // User-specific queries
    @Query("SELECT c FROM Complaint c WHERE c.complainant.id = :userId ORDER BY c.createdAt DESC")
    List<Complaint> findByComplainantId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Complaint c WHERE c.accused.id = :userId ORDER BY c.createdAt DESC")
    List<Complaint> findByAccusedId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Complaint c WHERE (c.complainant.id = :userId OR c.accused.id = :userId) ORDER BY c.createdAt DESC")
    List<Complaint> findByUserId(@Param("userId") Long userId);
    
    // Date range queries
    @Query("SELECT c FROM Complaint c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<Complaint> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT c FROM Complaint c WHERE c.resolvedAt BETWEEN :startDate AND :endDate")
    List<Complaint> findByResolvedDateRange(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    // Statistics queries
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status = :status")
    long countByStatus(@Param("status") ComplaintStatus status);
    
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.complainant = :user")
    long countByComplainant(@Param("user") User user);
    
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.accused = :user")
    long countByAccused(@Param("user") User user);
    
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.resolvedBy = :admin")
    long countByResolvedBy(@Param("admin") User admin);
    
    // Detailed fetch query
    @Query("SELECT c FROM Complaint c " +
           "JOIN FETCH c.serviceRequest sr " +
           "JOIN FETCH c.complainant comp " +
           "JOIN FETCH c.accused acc " +
           "LEFT JOIN FETCH c.resolvedBy rb " +
           "WHERE c.id = :id")
    Optional<Complaint> findByIdWithDetails(@Param("id") Long id);
    
    // Business logic queries
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Complaint c WHERE c.serviceRequest = :serviceRequest AND c.complainant = :complainant")
    boolean existsByServiceRequestAndComplainant(@Param("serviceRequest") ServiceRequest serviceRequest, 
                                               @Param("complainant") User complainant);
} 