package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.UserNotFoundException;
import com.fix4home.fix4home.exception.ResourceAlreadyExistsException;
import com.fix4home.fix4home.exception.BusinessValidationException;
import com.fix4home.fix4home.model.dto.chat.ConversationDTO;
import com.fix4home.fix4home.model.dto.chat.CreateConversationRequest;
import com.fix4home.fix4home.model.entity.*;
import com.fix4home.fix4home.model.enums.ConversationStatus;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing conversations in the chat system
 * Handles conversation creation, retrieval, and status management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService extends BaseService {
    
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServicePostRepository servicePostRepository;
    private final ConsultationRepository consultationRepository;

    // ==================== CONVERSATION MANAGEMENT ====================

    @Transactional
    public ConversationDTO createConversation(CreateConversationRequest request) {
        logBusinessOperation("CREATE_CONVERSATION", 
                "customerId=" + request.getCustomerId() + ", technicianId=" + request.getTechnicianId());

        validateRequired(request, "request");
        
        // Find participants
        User customer = findUserById(request.getCustomerId());
        User technician = findUserById(request.getTechnicianId());
        
        // Validate roles
        validateUserRole(customer, Role.CUSTOMER);
        validateUserRole(technician, Role.TECHNICIAN);
        
        // Check if conversation already exists for the business context
        if (conversationAlreadyExists(customer, technician, request)) {
            throw new ResourceAlreadyExistsException("Conversation already exists for this context");
        }
        
        // Build conversation
        Conversation.ConversationBuilder conversationBuilder = Conversation.builder()
                .customer(customer)
                .technician(technician)
                .status(ConversationStatus.ACTIVE);
        
        // Set business context
        if (request.getServiceRequestId() != null) {
            ServiceRequest serviceRequest = findServiceRequestById(request.getServiceRequestId());
            conversationBuilder.serviceRequest(serviceRequest);
        }
        if (request.getServicePostId() != null) {
            ServicePost servicePost = findServicePostById(request.getServicePostId());
            conversationBuilder.servicePost(servicePost);
        }
        if (request.getConsultationId() != null) {
            Consultation consultation = findConsultationById(request.getConsultationId());
            conversationBuilder.consultation(consultation);
        }
        
        Conversation conversation = conversationRepository.save(conversationBuilder.build());
        
        // Send initial system message if provided
        if (request.getInitialMessage() != null && !request.getInitialMessage().trim().isEmpty()) {
            // This will be handled by ChatService
        }
        
        log.info("Conversation created with ID: {}", conversation.getId());
        return convertConversationToDTO(conversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserConversations() {
        logBusinessOperation("GET_USER_CONVERSATIONS");
        
        User currentUser = getCurrentUser();
        List<Conversation> conversations = conversationRepository.findActiveConversationsByUser(currentUser);
        
        return conversations.stream()
                .map(this::convertConversationToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ConversationDTO> getUserConversationsPaginated(int page, int size) {
        logBusinessOperation("GET_USER_CONVERSATIONS_PAGINATED", "page=" + page);
        
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Conversation> conversationPage = conversationRepository
                .findByParticipantOrderByLastMessage(currentUser, pageable);
        
        return conversationPage.map(this::convertConversationToDTO);
    }

    @Transactional(readOnly = true)
    public ConversationDTO getConversationById(Long id) {
        logBusinessOperation("GET_CONVERSATION_BY_ID", "id=" + id);
        
        validatePositiveId(id, "id");
        Conversation conversation = findConversationById(id);
        User currentUser = getCurrentUser();
        
        // Check if user can access this conversation
        validateConversationAccess(conversation, currentUser);
        
        return convertConversationToDTO(conversation);
    }

    @Transactional
    public ConversationDTO archiveConversation(Long id) {
        logBusinessOperation("ARCHIVE_CONVERSATION", "id=" + id);
        
        validatePositiveId(id, "id");
        Conversation conversation = findConversationById(id);
        User currentUser = getCurrentUser();
        
        // Check if user can modify this conversation
        validateConversationAccess(conversation, currentUser);
        
        conversation.setStatus(ConversationStatus.ARCHIVED);
        Conversation savedConversation = conversationRepository.save(conversation);
        
        return convertConversationToDTO(savedConversation);
    }

    @Transactional
    public void updateLastMessageTime(Long conversationId) {
        Conversation conversation = findConversationById(conversationId);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    // ==================== BUSINESS INTEGRATION METHODS ====================

    @Transactional
    public ConversationDTO createConversationForServiceRequest(ServiceRequest serviceRequest) {
        logBusinessOperation("CREATE_CONVERSATION_FOR_SERVICE_REQUEST", 
                "serviceRequestId=" + serviceRequest.getId());

        CreateConversationRequest request = CreateConversationRequest.builder()
                .customerId(serviceRequest.getCustomer().getId())
                .technicianId(serviceRequest.getTechnician().getId())
                .serviceRequestId(serviceRequest.getId())
                .initialMessage("Service request has been accepted. You can now communicate directly!")
                .build();

        return createConversation(request);
    }

    @Transactional
    public ConversationDTO createConversationForServicePost(ServicePost servicePost, User technician) {
        logBusinessOperation("CREATE_CONVERSATION_FOR_SERVICE_POST", 
                "servicePostId=" + servicePost.getId() + ", technicianId=" + technician.getId());

        CreateConversationRequest request = CreateConversationRequest.builder()
                .customerId(servicePost.getCustomer().getId())
                .technicianId(technician.getId())
                .servicePostId(servicePost.getId())
                .initialMessage("Technician has responded to your service post. Let's discuss the details!")
                .build();

        return createConversation(request);
    }

    @Transactional
    public ConversationDTO createConversationForConsultation(Consultation consultation) {
        logBusinessOperation("CREATE_CONVERSATION_FOR_CONSULTATION", 
                "consultationId=" + consultation.getId());

        CreateConversationRequest request = CreateConversationRequest.builder()
                .customerId(consultation.getServicePost().getCustomer().getId())
                .technicianId(consultation.getTechnician().getId())
                .consultationId(consultation.getId())
                .initialMessage("Consultation has been submitted. Let's discuss your requirements!")
                .build();

        return createConversation(request);
    }

    // ==================== HELPER METHODS ====================

    private boolean conversationAlreadyExists(User customer, User technician, CreateConversationRequest request) {
        return conversationRepository.existsByParticipantsAndBusinessContext(
                customer, technician,
                request.getServiceRequestId(),
                request.getServicePostId(),
                request.getConsultationId()
        );
    }

    private Conversation findConversationById(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Conversation not found with ID: " + id));
    }

    private ServiceRequest findServiceRequestById(Long id) {
        return serviceRequestRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Service request not found with ID: " + id));
    }

    private ServicePost findServicePostById(Long id) {
        return servicePostRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Service post not found with ID: " + id));
    }

    private Consultation findConsultationById(Long id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Consultation not found with ID: " + id));
    }

    private void validateConversationAccess(Conversation conversation, User user) {
        if (!conversation.canUserAccess(user)) {
            throw new BusinessValidationException("You don't have access to this conversation");
        }
    }

    private ConversationDTO convertConversationToDTO(Conversation conversation) {
        // Get unread count for current user
        User currentUser = getCurrentUser();
        long unreadCount = messageRepository.countUnreadByConversationAndUser(conversation, currentUser);
        
        // Get last message
        Optional<Message> lastMessage = messageRepository.findFirstByConversationOrderBySentAtDesc(conversation);
        
        ConversationDTO.MessagePreviewDTO lastMessageDTO = null;
        if (lastMessage.isPresent()) {
            Message msg = lastMessage.get();
            lastMessageDTO = ConversationDTO.MessagePreviewDTO.builder()
                    .id(msg.getId())
                    .content(msg.getContent())
                    .messageType(msg.getMessageType().toString())
                    .senderName(msg.getSender().getUsername())
                    .sentAt(msg.getSentAt())
                    .isRead(msg.getIsRead())
                    .build();
        }
        
        return ConversationDTO.builder()
                .id(conversation.getId())
                .status(conversation.getStatus())
                .lastMessageAt(conversation.getLastMessageAt())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .serviceRequestId(conversation.getServiceRequest() != null ? conversation.getServiceRequest().getId() : null)
                .servicePostId(conversation.getServicePost() != null ? conversation.getServicePost().getId() : null)
                .consultationId(conversation.getConsultation() != null ? conversation.getConsultation().getId() : null)
                .customer(convertUserToParticipantDTO(conversation.getCustomer()))
                .technician(convertUserToParticipantDTO(conversation.getTechnician()))
                .lastMessage(lastMessageDTO)
                .unreadCount((int) unreadCount)
                .build();
    }

    private ConversationDTO.ParticipantDTO convertUserToParticipantDTO(User user) {
        return ConversationDTO.ParticipantDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getUsername()) // TODO: Get from profile when needed
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .isOnline(false) // TODO: Implement online status tracking
                .build();
    }
} 