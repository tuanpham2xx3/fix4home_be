package com.fix4home.fix4home.service;

import com.fix4home.fix4home.model.entity.*;
import com.fix4home.fix4home.model.enums.ServiceRequestStatus;
import com.fix4home.fix4home.model.enums.ServicePostStatus;
import com.fix4home.fix4home.model.enums.ConsultationStatus;
import com.fix4home.fix4home.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Event listener for chat system integration
 * Automatically creates conversations when business events occur
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatEventListener extends BaseService {
    
    private final ConversationService conversationService;
    private final ChatService chatService;
    private final ConversationRepository conversationRepository;

    // ==================== SERVICE REQUEST EVENTS ====================

    /**
     * Create conversation when service request is accepted by technician
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleServiceRequestAccepted(ServiceRequestAcceptedEvent event) {
        log.info("Handling ServiceRequest accepted event: {}", event.getServiceRequestId());
        
        try {
            ServiceRequest serviceRequest = event.getServiceRequest();
            
            // Check if conversation already exists
            Optional<Conversation> existingConversation = conversationRepository
                    .findByServiceRequestId(serviceRequest.getId());
            
            if (existingConversation.isEmpty()) {
                // Create new conversation
                conversationService.createConversationForServiceRequest(serviceRequest);
                
                // Send initial system message
                Conversation conversation = conversationRepository
                        .findByServiceRequestId(serviceRequest.getId()).orElseThrow();
                
                chatService.sendSystemMessage(
                    conversation.getId(),
                    String.format("Service request has been accepted by technician %s. You can now communicate directly about the work details!",
                            serviceRequest.getTechnician().getUsername())
                );
                
                log.info("Created conversation for ServiceRequest: {}", serviceRequest.getId());
            }
        } catch (Exception e) {
            log.error("Error creating conversation for ServiceRequest {}: {}", 
                    event.getServiceRequestId(), e.getMessage());
        }
    }

    /**
     * Send notification when work starts
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleServiceRequestStarted(ServiceRequestStartedEvent event) {
        log.info("Handling ServiceRequest started event: {}", event.getServiceRequestId());
        
        try {
            Optional<Conversation> conversation = conversationRepository
                    .findByServiceRequestId(event.getServiceRequestId());
            
            if (conversation.isPresent()) {
                chatService.sendSystemMessage(
                    conversation.get().getId(),
                    "Work has started on your service request. The technician is now on-site!"
                );
            }
        } catch (Exception e) {
            log.error("Error sending work started message for ServiceRequest {}: {}", 
                    event.getServiceRequestId(), e.getMessage());
        }
    }

    /**
     * Send notification and archive conversation when work completes
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleServiceRequestCompleted(ServiceRequestCompletedEvent event) {
        log.info("Handling ServiceRequest completed event: {}", event.getServiceRequestId());
        
        try {
            Optional<Conversation> conversation = conversationRepository
                    .findByServiceRequestId(event.getServiceRequestId());
            
            if (conversation.isPresent()) {
                // Send completion message
                chatService.sendSystemMessage(
                    conversation.get().getId(),
                    "Work has been completed! Please review the service and provide feedback. This conversation will be archived in 24 hours."
                );
                
                // Schedule conversation archival (can be implemented with scheduled task)
                scheduleConversationArchival(conversation.get().getId(), Duration.ofHours(24));
            }
        } catch (Exception e) {
            log.error("Error handling completed ServiceRequest {}: {}", 
                    event.getServiceRequestId(), e.getMessage());
        }
    }

    // ==================== SERVICE POST EVENTS ====================

    /**
     * Create conversation when technician responds to service post
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleServicePostResponseCreated(ServicePostResponseCreatedEvent event) {
        log.info("Handling ServicePost response created event: {}", event.getServicePostId());
        
        try {
            ServicePostResponse response = event.getServicePostResponse();
            ServicePost servicePost = response.getServicePost();
            User technician = response.getTechnician();
            
            // Check if conversation already exists between these participants for this post
            Optional<Conversation> existingConversation = conversationRepository
                    .findByServicePostId(servicePost.getId());
            
            if (existingConversation.isEmpty()) {
                // Create conversation
                conversationService.createConversationForServicePost(servicePost, technician);
                
                // Send initial system message
                Conversation conversation = conversationRepository
                        .findByServicePostId(servicePost.getId()).orElseThrow();
                
                chatService.sendSystemMessage(
                    conversation.getId(),
                    String.format("Technician %s has responded to your service post with a quote of %s. You can discuss the details here!",
                            technician.getUsername(),
                            response.getQuotedPrice() != null ? "$" + response.getQuotedPrice() : "custom pricing")
                );
                
                log.info("Created conversation for ServicePost response: {}", response.getId());
            }
        } catch (Exception e) {
            log.error("Error creating conversation for ServicePost response {}: {}", 
                    event.getServicePostResponseId(), e.getMessage());
        }
    }

    /**
     * Notify when technician is selected for service post
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleServicePostTechnicianSelected(ServicePostTechnicianSelectedEvent event) {
        log.info("Handling ServicePost technician selected event: {}", event.getServicePostId());
        
        try {
            Optional<Conversation> conversation = conversationRepository
                    .findByServicePostId(event.getServicePostId());
            
            if (conversation.isPresent()) {
                chatService.sendSystemMessage(
                    conversation.get().getId(),
                    "Congratulations! You have been selected for this service post. Please coordinate with the customer to schedule the work."
                );
            }
        } catch (Exception e) {
            log.error("Error sending technician selected message for ServicePost {}: {}", 
                    event.getServicePostId(), e.getMessage());
        }
    }

    // ==================== CONSULTATION EVENTS ====================

    /**
     * Create conversation when consultation is submitted
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleConsultationSubmitted(ConsultationSubmittedEvent event) {
        log.info("Handling Consultation submitted event: {}", event.getConsultationId());
        
        try {
            Consultation consultation = event.getConsultation();
            
            // Check if conversation already exists
            Optional<Conversation> existingConversation = conversationRepository
                    .findByConsultationId(consultation.getId());
            
            if (existingConversation.isEmpty()) {
                // Create conversation
                conversationService.createConversationForConsultation(consultation);
                
                // Send initial system message
                Conversation conversation = conversationRepository
                        .findByConsultationId(consultation.getId()).orElseThrow();
                
                chatService.sendSystemMessage(
                    conversation.getId(),
                    String.format("Consultation has been submitted with a quote of %s. Please discuss any questions or modifications here!",
                            consultation.getQuotedPrice() != null ? "$" + consultation.getQuotedPrice() : "custom pricing")
                );
                
                log.info("Created conversation for Consultation: {}", consultation.getId());
            }
        } catch (Exception e) {
            log.error("Error creating conversation for Consultation {}: {}", 
                    event.getConsultationId(), e.getMessage());
        }
    }

    /**
     * Notify when consultation is accepted
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleConsultationAccepted(ConsultationAcceptedEvent event) {
        log.info("Handling Consultation accepted event: {}", event.getConsultationId());
        
        try {
            Optional<Conversation> conversation = conversationRepository
                    .findByConsultationId(event.getConsultationId());
            
            if (conversation.isPresent()) {
                chatService.sendSystemMessage(
                    conversation.get().getId(),
                    "Your consultation has been accepted! Please proceed to schedule the service or create a service request."
                );
            }
        } catch (Exception e) {
            log.error("Error sending consultation accepted message for Consultation {}: {}", 
                    event.getConsultationId(), e.getMessage());
        }
    }

    // ==================== COMPLAINT EVENTS ====================

    /**
     * Notify about complaint creation in related conversation
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleComplaintCreated(ComplaintCreatedEvent event) {
        log.info("Handling Complaint created event: {}", event.getComplaintId());
        
        try {
            Complaint complaint = event.getComplaint();
            ServiceRequest serviceRequest = complaint.getServiceRequest();
            
            Optional<Conversation> conversation = conversationRepository
                    .findByServiceRequestId(serviceRequest.getId());
            
            if (conversation.isPresent()) {
                chatService.sendSystemMessage(
                    conversation.get().getId(),
                    "A complaint has been filed regarding this service. Communication is being monitored by administrators for resolution."
                );
            }
        } catch (Exception e) {
            log.error("Error sending complaint created message for Complaint {}: {}", 
                    event.getComplaintId(), e.getMessage());
        }
    }

    // ==================== HELPER METHODS ====================

    private void scheduleConversationArchival(Long conversationId, Duration delay) {
        // This could be implemented with a scheduled task service
        // For now, just log the intention
        log.info("Scheduled archival for conversation {} in {} hours", 
                conversationId, delay.toHours());
        
        // TODO: Implement actual scheduling mechanism
        // Could use Spring's @Scheduled or external job scheduler
    }

    // ==================== EVENT CLASSES ====================

    /**
     * Event fired when a service request is accepted by a technician
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ServiceRequestAcceptedEvent {
        private Long serviceRequestId;
        private ServiceRequest serviceRequest;
        private LocalDateTime timestamp;
    }

    /**
     * Event fired when work starts on a service request
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ServiceRequestStartedEvent {
        private Long serviceRequestId;
        private LocalDateTime timestamp;
    }

    /**
     * Event fired when a service request is completed
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ServiceRequestCompletedEvent {
        private Long serviceRequestId;
        private LocalDateTime timestamp;
    }

    /**
     * Event fired when a technician responds to a service post
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ServicePostResponseCreatedEvent {
        private Long servicePostResponseId;
        private Long servicePostId;
        private ServicePostResponse servicePostResponse;
        private LocalDateTime timestamp;
    }

    /**
     * Event fired when a technician is selected for a service post
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ServicePostTechnicianSelectedEvent {
        private Long servicePostId;
        private Long technicianId;
        private LocalDateTime timestamp;
    }

    /**
     * Event fired when a consultation is submitted
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ConsultationSubmittedEvent {
        private Long consultationId;
        private Consultation consultation;
        private LocalDateTime timestamp;
    }

    /**
     * Event fired when a consultation is accepted
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ConsultationAcceptedEvent {
        private Long consultationId;
        private LocalDateTime timestamp;
    }

    /**
     * Event fired when a complaint is created
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ComplaintCreatedEvent {
        private Long complaintId;
        private Complaint complaint;
        private LocalDateTime timestamp;
    }
} 