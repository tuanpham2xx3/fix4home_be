package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.BusinessValidationException;
import com.fix4home.fix4home.exception.UserNotFoundException;
import com.fix4home.fix4home.model.dto.chat.MessageDTO;
import com.fix4home.fix4home.model.dto.chat.SendMessageRequest;
import com.fix4home.fix4home.model.dto.chat.MarkAsReadRequest;
import com.fix4home.fix4home.model.entity.Conversation;
import com.fix4home.fix4home.model.entity.Message;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.MessageType;
import com.fix4home.fix4home.repository.ConversationRepository;
import com.fix4home.fix4home.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing chat messages and real-time messaging
 * Handles message sending, receiving, and status management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService extends BaseService {
    
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatbotService chatbotService;

    // ==================== MESSAGE MANAGEMENT ====================

    @Transactional
    public MessageDTO sendMessage(SendMessageRequest request, String senderUsername) {
        logBusinessOperation("SEND_MESSAGE", "conversationId=" + request.getConversationId());
        
        validateRequired(request, "request");
        
        // Find conversation and validate access
        Conversation conversation = findConversationById(request.getConversationId());
        User sender = getCurrentUser();
        
        // Validate sender can access this conversation
        validateConversationAccess(conversation, sender);
        
        // Validate conversation is active
        if (!conversation.isActive()) {
            throw new BusinessValidationException("Cannot send message to archived or blocked conversation");
        }
        
        // Create message
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .messageType(request.getMessageType())
                .attachmentUrl(request.getAttachmentUrl())
                .metadata(request.getMetadata())
                .build();
        
        Message savedMessage = messageRepository.save(message);
        
        // Update conversation last message time
        conversationService.updateLastMessageTime(conversation.getId());
        
        // Convert to DTO for response and WebSocket broadcast
        MessageDTO messageDTO = convertMessageToDTO(savedMessage);
        
        // Check if this is a chatbot conversation and send to n8n
        if (chatbotService.isChatbotConversation(conversation)) {
            log.info("Detected chatbot conversation, sending message to n8n");
            chatbotService.sendToN8n(savedMessage);
        }
        
        // Send real-time message to participants
        broadcastMessageToParticipants(conversation, messageDTO);
        
        log.info("Message sent with ID: {} in conversation: {}", savedMessage.getId(), conversation.getId());
        return messageDTO;
    }

    @Transactional
    public MessageDTO sendSystemMessage(Long conversationId, String content) {
        logBusinessOperation("SEND_SYSTEM_MESSAGE", "conversationId=" + conversationId);
        
        Conversation conversation = findConversationById(conversationId);
        
        // Create system message (no sender validation needed)
        Message message = Message.builder()
                .conversation(conversation)
                .sender(null) // System messages have no sender
                .content(content)
                .messageType(MessageType.SYSTEM)
                .isRead(false)
                .build();
        
        Message savedMessage = messageRepository.save(message);
        
        // Update conversation last message time
        conversationService.updateLastMessageTime(conversation.getId());
        
        // Convert to DTO and broadcast
        MessageDTO messageDTO = convertSystemMessageToDTO(savedMessage);
        broadcastMessageToParticipants(conversation, messageDTO);
        
        return messageDTO;
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> getConversationMessages(Long conversationId, int page, int size) {
        logBusinessOperation("GET_CONVERSATION_MESSAGES", "conversationId=" + conversationId);
        
        Conversation conversation = findConversationById(conversationId);
        User currentUser = getCurrentUser();
        
        // Validate access
        validateConversationAccess(conversation, currentUser);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = messageRepository.findByConversationOrderBySentAtDesc(conversation, pageable);
        
        return messagePage.map(this::convertMessageToDTO);
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> getMessagesBefore(Long conversationId, LocalDateTime beforeDate, int size) {
        logBusinessOperation("GET_MESSAGES_BEFORE", "conversationId=" + conversationId);
        
        Conversation conversation = findConversationById(conversationId);
        User currentUser = getCurrentUser();
        
        // Validate access
        validateConversationAccess(conversation, currentUser);
        
        Pageable pageable = PageRequest.of(0, size);
        Page<Message> messagePage = messageRepository.findByConversationBeforeDate(conversation, beforeDate, pageable);
        
        return messagePage.map(this::convertMessageToDTO);
    }

    @Transactional
    public void markMessagesAsRead(MarkAsReadRequest request) {
        logBusinessOperation("MARK_MESSAGES_AS_READ", "conversationId=" + request.getConversationId());
        
        validateRequired(request, "request");
        
        Conversation conversation = findConversationById(request.getConversationId());
        User currentUser = getCurrentUser();
        
        // Validate access
        validateConversationAccess(conversation, currentUser);
        
        int updatedCount;
        
        if (request.getMessageIds() != null && !request.getMessageIds().isEmpty()) {
            // Mark specific messages as read
            updatedCount = messageRepository.markMessagesAsRead(request.getMessageIds());
        } else {
            // Mark all unread messages in conversation as read for current user
            updatedCount = messageRepository.markAllAsReadForUserInConversation(conversation, currentUser);
        }
        
        log.info("Marked {} messages as read in conversation {}", updatedCount, conversation.getId());
        
        // Notify other participant about read status update
        broadcastReadStatusUpdate(conversation, currentUser);
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getUnreadMessagesForUser() {
        logBusinessOperation("GET_UNREAD_MESSAGES_FOR_USER");
        
        User currentUser = getCurrentUser();
        List<Message> unreadMessages = messageRepository.findUnreadMessagesForUser(currentUser);
        
        return unreadMessages.stream()
                .map(this::convertMessageToDTO)
                .toList();
    }

    // ==================== REAL-TIME MESSAGING ====================

    public void broadcastMessageToParticipants(Conversation conversation, MessageDTO messageDTO) {
        // Send to customer
        String customerDestination = "/user/" + conversation.getCustomer().getUsername() + "/queue/messages";
        messagingTemplate.convertAndSend(customerDestination, messageDTO);
        
        // Send to technician
        String technicianDestination = "/user/" + conversation.getTechnician().getUsername() + "/queue/messages";
        messagingTemplate.convertAndSend(technicianDestination, messageDTO);
        
        log.debug("Message broadcasted to conversation participants");
    }

    private void broadcastReadStatusUpdate(Conversation conversation, User reader) {
        // Create read status update message
        ReadStatusUpdateDTO updateDTO = ReadStatusUpdateDTO.builder()
                .conversationId(conversation.getId())
                .readerUsername(reader.getUsername())
                .readAt(LocalDateTime.now())
                .build();
        
        // Get the other participant
        User otherParticipant = conversation.getOtherParticipant(reader);
        String destination = "/user/" + otherParticipant.getUsername() + "/queue/read-status";
        messagingTemplate.convertAndSend(destination, updateDTO);
    }

    // ==================== HELPER METHODS ====================

    private Conversation findConversationById(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Conversation not found with ID: " + id));
    }

    private void validateConversationAccess(Conversation conversation, User user) {
        if (!conversation.canUserAccess(user)) {
            throw new BusinessValidationException("You don't have access to this conversation");
        }
    }

    public MessageDTO convertMessageToDTO(Message message) {
        MessageDTO.SenderDTO senderDTO = null;
        if (message.getSender() != null) {
            senderDTO = MessageDTO.SenderDTO.builder()
                    .userId(message.getSender().getId())
                    .username(message.getSender().getUsername())
                    .fullName(message.getSender().getUsername()) // TODO: Get from profile
                    .role(message.getSender().getRole().toString())
                    .build();
        }
        
        return MessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .attachmentUrl(message.getAttachmentUrl())
                .metadata(message.getMetadata())
                .isRead(message.getIsRead())
                .sentAt(message.getSentAt())
                .sender(senderDTO)
                .build();
    }

    private MessageDTO convertSystemMessageToDTO(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .attachmentUrl(message.getAttachmentUrl())
                .metadata(message.getMetadata())
                .isRead(message.getIsRead())
                .sentAt(message.getSentAt())
                .sender(null) // System messages have no sender
                .build();
    }

    /**
     * DTO for read status updates
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class ReadStatusUpdateDTO {
        private Long conversationId;
        private String readerUsername;
        private LocalDateTime readAt;
    }
} 