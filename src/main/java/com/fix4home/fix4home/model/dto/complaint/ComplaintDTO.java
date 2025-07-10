package com.fix4home.fix4home.model.dto.complaint;

import com.fix4home.fix4home.model.dto.servicerequest.ServiceRequestSummaryDTO;
import com.fix4home.fix4home.model.enums.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintDTO {
    
    private Long id;
    private Long serviceRequestId;
    private ServiceRequestSummaryDTO serviceRequest;
    private Long complainantId;
    private String complainantName;
    private String complainantPhone;
    private String complainantEmail;
    private Long accusedId;
    private String accusedName;
    private String accusedPhone;
    private String accusedEmail;
    private String reason;
    private String description;
    private ComplaintStatus status;
    private String adminResponse;
    private Long resolvedById;
    private String resolvedByName;
    private String resolvedByEmail;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    
    // Computed fields
    private Boolean isPending;
    private Boolean isInvestigating;
    private Boolean isResolved;
    private Boolean isRejected;
    private Boolean canBeModified;
    private Boolean canBeInvestigated;
    private Boolean canBeResolved;
} 