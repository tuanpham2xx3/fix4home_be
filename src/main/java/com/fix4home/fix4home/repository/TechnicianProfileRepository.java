package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.TechnicianProfile;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianProfileRepository extends JpaRepository<TechnicianProfile, Long> {
    
    Optional<TechnicianProfile> findByUser(User user);
    
    Optional<TechnicianProfile> findByUserId(Long userId);
    
    List<TechnicianProfile> findByStatus(UserStatus status);
} 