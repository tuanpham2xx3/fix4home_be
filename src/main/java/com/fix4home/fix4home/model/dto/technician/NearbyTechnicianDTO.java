package com.fix4home.fix4home.model.dto.technician;

import com.fix4home.fix4home.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyTechnicianDTO {
    
    // Basic technician info
    private Long userId;
    private Long profileId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Float rating;
    private UserStatus status;
    
    // Location info
    private Double currentLatitude;
    private Double currentLongitude;
    private String currentAddress;
    private Integer workingRadius;
    private Double distanceKm; // Distance from search point
    
    // Online status
    private Boolean isOnline;
    private LocalDateTime lastSeenAt;
    
    // Skills
    private List<SkillDTO> skillList;
} 