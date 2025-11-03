package com.fix4home.fix4home.model.dto.technician;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillDTO {
    
    private Long id;
    private String name;
} 