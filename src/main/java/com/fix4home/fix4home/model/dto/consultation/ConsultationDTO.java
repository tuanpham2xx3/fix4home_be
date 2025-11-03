package com.fix4home.fix4home.model.dto.consultation;

import com.fix4home.fix4home.model.dto.servicepost.ServicePostSummaryDTO;
import com.fix4home.fix4home.model.enums.ConsultationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationDTO {
    
    private Long id;
    private Long servicePostId;
    private ServicePostSummaryDTO servicePost;
    private Long technicianId;
    private String technicianName;
    private String technicianPhone;
    private String technicianEmail;
    private Double technicianRating;
    private String proposal;
    private BigDecimal quotedPrice;
    private String notes;
    private ConsultationStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime respondedAt;
    
    // Computed fields
    private Boolean isPending;
    private Boolean isAccepted;
    private Boolean isRejected;
    private Boolean canBeModified;
} 