package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.ServiceRequest;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.ServiceRequestStatus;
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
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    
    List<ServiceRequest> findByCustomer(User customer);
    
    List<ServiceRequest> findByTechnician(User technician);
    
    List<ServiceRequest> findByStatus(ServiceRequestStatus status);
    
    List<ServiceRequest> findByCustomerAndStatus(User customer, ServiceRequestStatus status);
    
    List<ServiceRequest> findByTechnicianAndStatus(User technician, ServiceRequestStatus status);
    
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.technician IS NULL AND sr.status = :status")
    List<ServiceRequest> findUnassignedRequests(@Param("status") ServiceRequestStatus status);
    
    @Query("SELECT COUNT(sr) FROM ServiceRequest sr WHERE sr.technician = :technician AND sr.status IN :statuses")
    long countByTechnicianAndStatusIn(@Param("technician") User technician, @Param("statuses") List<ServiceRequestStatus> statuses);
    
    // Enhanced queries for ServiceRequestController
    Page<ServiceRequest> findByCustomer(User customer, Pageable pageable);
    
    Page<ServiceRequest> findByTechnician(User technician, Pageable pageable);
    
    Page<ServiceRequest> findByStatus(ServiceRequestStatus status, Pageable pageable);
    
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.technician IS NULL AND sr.status = :status ORDER BY sr.createdAt ASC")
    List<ServiceRequest> findAvailableRequests(@Param("status") ServiceRequestStatus status);
    
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.customer.id = :customerId ORDER BY sr.createdAt DESC")
    List<ServiceRequest> findByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.technician.id = :technicianId ORDER BY sr.createdAt DESC")
    List<ServiceRequest> findByTechnicianId(@Param("technicianId") Long technicianId);
    
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.service.id = :serviceId")
    List<ServiceRequest> findByServiceId(@Param("serviceId") Long serviceId);
    
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.createdAt BETWEEN :startDate AND :endDate")
    List<ServiceRequest> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(sr) FROM ServiceRequest sr WHERE sr.status = :status")
    long countByStatus(@Param("status") ServiceRequestStatus status);
    
    @Query("SELECT sr FROM ServiceRequest sr JOIN FETCH sr.customer JOIN FETCH sr.service LEFT JOIN FETCH sr.technician JOIN FETCH sr.address WHERE sr.id = :id")
    ServiceRequest findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT AVG(sr.price) FROM ServiceRequest sr")
    Optional<BigDecimal> findAveragePrice();
} 