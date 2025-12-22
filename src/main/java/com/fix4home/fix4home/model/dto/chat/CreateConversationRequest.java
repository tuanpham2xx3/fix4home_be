package com.fix4home.fix4home.model.dto.chat;

import com.fix4home.fix4home.model.enums.ConversationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating new conversations
 * Used internally by the system when business events trigger conversation creation
 * Also supports free chat and chatbot conversations
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
    
    // Business context - one of these should be set for BUSINESS type
    private Long serviceRequestId;
    private Long servicePostId;
    private Long consultationId;
    
    // Conversation type - defaults to BUSINESS for backward compatibility
    @Builder.Default
    private ConversationType conversationType = ConversationType.BUSINESS;
    
    // For free chat - alternative to customerId/technicianId
    private Long otherUserId;
    
    // Optional initial system message
    private String initialMessage;
} 