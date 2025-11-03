package com.fix4home.fix4home.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "technician_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TechnicianSkillId.class)
public class TechnicianSkill {
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_profile_id", nullable = false)
    private TechnicianProfile technicianProfile;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;
} 