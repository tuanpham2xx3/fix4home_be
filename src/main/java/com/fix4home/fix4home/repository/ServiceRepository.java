package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.Service;
import com.fix4home.fix4home.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    List<Service> findByStatus(UserStatus status);
    
    List<Service> findByNameContainingIgnoreCase(String name);
    
    boolean existsByName(String name);
} 