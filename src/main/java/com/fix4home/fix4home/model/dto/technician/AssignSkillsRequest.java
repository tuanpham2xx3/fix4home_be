package com.fix4home.fix4home.model.dto.technician;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignSkillsRequest {
    
    @NotNull(message = "Skill IDs list is required")
    @NotEmpty(message = "At least one skill ID is required")
    private List<Long> skillIds;
} 