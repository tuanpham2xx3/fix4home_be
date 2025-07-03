package com.fix4home.fix4home.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianSkillId implements Serializable {
    
    private Long technicianProfile;
    private Long skill;
} 