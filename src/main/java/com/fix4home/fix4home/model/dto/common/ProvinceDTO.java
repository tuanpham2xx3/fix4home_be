package com.fix4home.fix4home.model.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvinceDTO {
    
    private String code;
    private String name;
    private String slug;
    private String type;
    private String nameWithType;
    private String codeName;
} 