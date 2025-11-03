package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.Skill;
import com.fix4home.fix4home.model.entity.TechnicianProfile;
import com.fix4home.fix4home.model.entity.TechnicianSkill;
import com.fix4home.fix4home.model.entity.TechnicianSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianSkillRepository extends JpaRepository<TechnicianSkill, TechnicianSkillId> {
    
    List<TechnicianSkill> findByTechnicianProfile(TechnicianProfile technicianProfile);
    
    List<TechnicianSkill> findByTechnicianProfileId(Long technicianProfileId);
    
    List<TechnicianSkill> findBySkill(Skill skill);
    
    List<TechnicianSkill> findBySkillId(Long skillId);
    
    @Query("SELECT ts FROM TechnicianSkill ts WHERE ts.technicianProfile.id = :technicianProfileId AND ts.skill.id = :skillId")
    TechnicianSkill findByTechnicianProfileIdAndSkillId(@Param("technicianProfileId") Long technicianProfileId, @Param("skillId") Long skillId);
    
    boolean existsByTechnicianProfileAndSkill(TechnicianProfile technicianProfile, Skill skill);
    
    @Modifying
    @Query("DELETE FROM TechnicianSkill ts WHERE ts.technicianProfile.id = :technicianProfileId")
    void deleteByTechnicianProfileId(@Param("technicianProfileId") Long technicianProfileId);
    
    @Modifying
    @Query("DELETE FROM TechnicianSkill ts WHERE ts.technicianProfile.id = :technicianProfileId AND ts.skill.id = :skillId")
    void deleteByTechnicianProfileIdAndSkillId(@Param("technicianProfileId") Long technicianProfileId, @Param("skillId") Long skillId);
    
    @Modifying
    @Query("DELETE FROM TechnicianSkill ts WHERE ts.skill.id = :skillId")
    void deleteBySkillId(@Param("skillId") Long skillId);
} 