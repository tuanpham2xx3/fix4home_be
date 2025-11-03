package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.Payment;
import com.fix4home.fix4home.model.entity.ServiceRequest;
import com.fix4home.fix4home.model.enums.PaymentMethod;
import com.fix4home.fix4home.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Basic queries
    Optional<Payment> findByServiceRequest(ServiceRequest serviceRequest);
    
    @Query("SELECT p FROM Payment p WHERE p.serviceRequest.customer.id = :customerId")
    Page<Payment> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.serviceRequest.technician.id = :technicianId")
    Page<Payment> findByTechnicianId(@Param("technicianId") Long technicianId, Pageable pageable);
    
    List<Payment> findByStatus(PaymentStatus status);
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.serviceRequest.customer.id = :customerId AND p.status = :status")
    List<Payment> findByCustomerIdAndStatus(@Param("customerId") Long customerId, @Param("status") PaymentStatus status);
    
    // Count queries
    long countByStatus(PaymentStatus status);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.serviceRequest.customer.id = :customerId")
    long countByCustomerId(@Param("customerId") Long customerId);
    
    // Revenue calculations
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID'")
    BigDecimal calculateTotalRevenue();
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' AND p.paymentTime BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' AND p.serviceRequest.technician.id = :technicianId")
    BigDecimal calculateTechnicianEarnings(@Param("technicianId") Long technicianId);
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' AND p.serviceRequest.customer.id = :customerId")
    BigDecimal calculateCustomerTotalSpending(@Param("customerId") Long customerId);
    
    // Validation queries
    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.serviceRequest.id = :serviceRequestId")
    boolean existsByServiceRequestId(@Param("serviceRequestId") Long serviceRequestId);
} 