package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.CustomerProfile;
import com.fix4home.fix4home.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
    
    Optional<CustomerProfile> findByUser(User user);
    
    Optional<CustomerProfile> findByUserId(Long userId);
} 