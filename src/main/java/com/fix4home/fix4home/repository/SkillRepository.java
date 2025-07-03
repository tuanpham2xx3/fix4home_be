package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    
    Optional<Skill> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Skill> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT s FROM Skill s ORDER BY s.name ASC")
    List<Skill> findAllOrderByName();
    
    @Query("SELECT s FROM Skill s WHERE s.name IN :names")
    List<Skill> findByNameIn(@Param("names") List<String> names);
} 