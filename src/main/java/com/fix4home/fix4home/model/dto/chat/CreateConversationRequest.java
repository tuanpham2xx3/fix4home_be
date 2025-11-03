package com.fix4home.fix4home.model.dto.chat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating new conversations
 * Used internally by the system when business events trigger conversation creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateConversationRequest {
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Technician ID is required")
    private Long technicianId;
    
    // Business context - one of these should be set
    private Long serviceRequestId;
    private Long servicePostId;
    private Long consultationId;
    
    // Optional initial system message
    private String initialMessage;
} 