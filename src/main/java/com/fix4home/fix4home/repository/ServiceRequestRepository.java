package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.ServiceRequest;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.ServiceRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
} 