package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.BusinessValidationException;
import com.fix4home.fix4home.exception.UserNotFoundException;
import com.fix4home.fix4home.model.dto.chatbot.ChatbotSendResponse;
import com.fix4home.fix4home.model.entity.Conversation;
import com.fix4home.fix4home.model.entity.Message;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.ConversationType;
import com.fix4home.fix4home.model.enums.MessageType;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.repository.ConversationRepository;
import com.fix4home.fix4home.repository.MessageRepository;
import com.fix4home.fix4home.repository.UserRepository;
import com.fix4home.fix4home.model.dto.chat.MessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service for handling chatbot messages and n8n integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService extends BaseService {
    
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Value("${chatbot.enabled:true}")
    private boolean chatbotEnabled;
    
    @Value("${chatbot.user.username:chatbot_support}")
    private String chatbotUsername;
    
    @Value("${chatbot.n8n.webhook.url:}")
    private String n8nWebhookUrl;
    
    @Value("${chatbot.n8n.webhook.base.url:http://localhost:5678/webhook}")
    private String n8nWebhookBaseUrl;
    
    @Value("${chatbot.n8n.timeout:30000}")
    private int n8nTimeout;
    
    @Value("${chatbot.session.max.requests:5}")
    private int maxRequestsPerSession;
    
    /**
     * Check if a user is the chatbot user
     */
    @Transactional(readOnly = true)
    public boolean isChatbotUser(User user) {
        if (user == null) {
            return false;
        }
        return chatbotUsername.equals(user.getUsername()) && user.getRole() == Role.ADMIN;
    }
    
    /**
     * Get the chatbot user instance
     */
    @Transactional(readOnly = true)
    public User getChatbotUser() {
        return userRepository.findByUsernameAndRole(chatbotUsername, Role.ADMIN)
                .orElseThrow(() -> new UserNotFoundException("Chatbot user not found: " + chatbotUsername));
    }
    
    /**
     * Check if a conversation is a chatbot conversation
     */
    @Transactional(readOnly = true)
    public boolean isChatbotConversation(Conversation conversation) {
        return conversation != null && conversation.getConversationType() == ConversationType.CHATBOT;
    }
    
    /**
     * Send message to n8n webhook asynchronously
     */
    @Async
    @Transactional
    public void sendToN8n(Message message) {
        if (!chatbotEnabled || n8nWebhookUrl == null || n8nWebhookUrl.trim().isEmpty()) {
            log.warn("Chatbot is disabled or n8n webhook URL not configured");
            return;
        }
        
        try {
            Conversation conversation = message.getConversation();
            User sender = message.getSender();
            
            // Prepare payload for n8n
            Map<String, Object> payload = new HashMap<>();
            payload.put("conversationId", conversation.getId());
            payload.put("userId", sender.getId());
            payload.put("username", sender.getUsername());
            payload.put("message", message.getContent());
            payload.put("messageId", message.getId());
            payload.put("timestamp", message.getSentAt().toString());
            
            // Add metadata if available
            if (message.getMetadata() != null && !message.getMetadata().trim().isEmpty()) {
                payload.put("metadata", message.getMetadata());
            }
            
            log.info("Sending message {} to n8n webhook: {}", message.getId(), n8nWebhookUrl);
            
            // Call n8n webhook asynchronously (fire and forget)
            restTemplate.postForObject(n8nWebhookUrl, payload, String.class);
            
            log.debug("Message {} sent to n8n successfully", message.getId());
            
        } catch (Exception e) {
            log.error("Error sending message {} to n8n webhook: {}", message.getId(), e.getMessage(), e);
            // Don't throw exception - we don't want to block the user's message
        }
    }
    
    /**
     * Process chatbot response from n8n webhook
     * This will be called by ChatbotWebhookController
     */
    @Transactional
    public Message processChatbotResponse(Long conversationId, String response, String metadata) {
        logBusinessOperation("PROCESS_CHATBOT_RESPONSE", "conversationId=" + conversationId);
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new UserNotFoundException("Conversation not found: " + conversationId));
        
        // Validate it's a chatbot conversation
        if (!isChatbotConversation(conversation)) {
            throw new IllegalArgumentException("Conversation is not a chatbot conversation");
        }
        
        User chatbotUser = getChatbotUser();
        
        // Create message from chatbot
        Message message = Message.builder()
                .conversation(conversation)
                .sender(chatbotUser)
                .content(response)
                .messageType(com.fix4home.fix4home.model.enums.MessageType.TEXT)
                .metadata(metadata)
                .isRead(false)
                .build();
        
        Message savedMessage = messageRepository.save(message);
        
        // Update conversation last message time
        conversationRepository.findById(conversationId).ifPresent(conv -> {
            conv.setLastMessageAt(java.time.LocalDateTime.now());
            conversationRepository.save(conv);
        });
        
        log.info("Chatbot response message created with ID: {} for conversation: {}", 
            savedMessage.getId(), conversationId);
        
        return savedMessage;
    }
    
    // ==================== SESSION MANAGEMENT WITH N8N ====================
    
    /**
     * Send message to n8n webhook with session management
     * Creates message in database, manages session limit (5 requests per session),
     * and sends to n8n with sessionId for node memory
     */
    @Transactional
    public ChatbotSendResponse sendMessageToN8nWithSession(Long conversationId, String sessionId, String messageContent) {
        logBusinessOperation("SEND_MESSAGE_TO_N8N_WITH_SESSION", 
            "conversationId=" + conversationId + ", sessionId=" + sessionId);
        
        validateRequired(conversationId, "conversationId");
        validateRequired(sessionId, "sessionId");
        validateRequired(messageContent, "messageContent");
        
        // Get current authenticated user
        User sender = getCurrentUser();
        
        // Find conversation and validate
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new UserNotFoundException("Conversation not found: " + conversationId));
        
        // Validate it's a chatbot conversation
        if (!isChatbotConversation(conversation)) {
            throw new BusinessValidationException("Conversation is not a chatbot conversation");
        }
        
        // Validate sender can access this conversation
        if (!conversation.canUserAccess(sender)) {
            throw new BusinessValidationException("You don't have access to this conversation");
        }
        
        // Create and save message to database
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(messageContent)
                .messageType(MessageType.TEXT)
                .isRead(false)
                .build();
        
        Message savedMessage = messageRepository.save(message);
        
        // Update conversation last message time
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        log.info("Message created with ID: {} for conversation: {}", savedMessage.getId(), conversationId);
        
        // Broadcast message via WebSocket for real-time updates
        try {
            MessageDTO messageDTO = convertMessageToDTO(savedMessage);
            broadcastMessageToParticipants(conversation, messageDTO);
            log.debug("Message broadcasted to conversation participants via WebSocket");
        } catch (Exception e) {
            log.warn("Failed to broadcast message via WebSocket: {}", e.getMessage());
            // Don't fail the request if WebSocket broadcast fails
        }
        
        // Manage session limit
        String finalSessionId = sessionId;
        boolean newSessionCreated = false;
        
        Integer currentCount = getSessionCount(sessionId);
        if (currentCount == null) {
            currentCount = 0;
        }
        
        if (currentCount >= maxRequestsPerSession) {
            // Create new session
            finalSessionId = generateNewSessionId();
            resetSessionCount(finalSessionId);
            newSessionCreated = true;
            log.info("Session limit exceeded for sessionId: {}. Created new sessionId: {}", sessionId, finalSessionId);
        } else {
            // Increment counter
            incrementSessionCount(sessionId);
        }
        
        // Send to n8n asynchronously (fire and forget)
        sendToN8nWithSession(finalSessionId, savedMessage, conversation, sender);
        
        return ChatbotSendResponse.builder()
                .sessionId(finalSessionId)
                .messageId(savedMessage.getId())
                .messageSent(true)
                .newSessionCreated(newSessionCreated)
                .build();
    }
    
    /**
     * Send message to n8n webhook with sessionId in URL
     */
    @Async
    public void sendToN8nWithSession(String sessionId, Message message, Conversation conversation, User sender) {
        if (!chatbotEnabled || n8nWebhookBaseUrl == null || n8nWebhookBaseUrl.trim().isEmpty()) {
            log.warn("Chatbot is disabled or n8n webhook base URL not configured");
            return;
        }
        
        try {
            String webhookUrl = n8nWebhookBaseUrl + "/" + sessionId;
            
            // Prepare payload for n8n
            Map<String, Object> payload = new HashMap<>();
            payload.put("message", message.getContent());
            payload.put("sessionId", sessionId);
            payload.put("conversationId", conversation.getId());
            payload.put("userId", sender.getId());
            payload.put("username", sender.getUsername());
            payload.put("messageId", message.getId());
            payload.put("timestamp", message.getSentAt().toString());
            
            // Add metadata if available
            if (message.getMetadata() != null && !message.getMetadata().trim().isEmpty()) {
                payload.put("metadata", message.getMetadata());
            }
            
            log.info("Sending message {} to n8n webhook: {} with sessionId: {}", message.getId(), webhookUrl, sessionId);
            
            // Call n8n webhook asynchronously (fire and forget)
            restTemplate.postForObject(webhookUrl, payload, String.class);
            
            log.debug("Message {} sent to n8n successfully with sessionId: {}", message.getId(), sessionId);
            
        } catch (Exception e) {
            log.error("Error sending message {} to n8n webhook with sessionId {}: {}", 
                message.getId(), sessionId, e.getMessage(), e);
            // Don't throw exception - we don't want to block the user's message
        }
    }
    
    /**
     * Get session count from Redis
     */
    private Integer getSessionCount(String sessionId) {
        try {
            String key = "chatbot:session:" + sessionId + ":count";
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            Object value = ops.get(key);
            
            if (value == null) {
                return null;
            }
            
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error getting session count for sessionId {}: {}", sessionId, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Increment session count in Redis
     */
    private void incrementSessionCount(String sessionId) {
        try {
            String key = "chatbot:session:" + sessionId + ":count";
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            
            // Get current value
            Integer currentCount = getSessionCount(sessionId);
            if (currentCount == null) {
                currentCount = 0;
            }
            
            // Increment and set
            Integer newValue = currentCount + 1;
            ops.set(key, newValue);
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
            
            log.debug("Incremented session count for sessionId: {} to {}", sessionId, newValue);
        } catch (Exception e) {
            log.error("Error incrementing session count for sessionId {}: {}", sessionId, e.getMessage(), e);
        }
    }
    
    /**
     * Reset session count to 1 for new session
     */
    private void resetSessionCount(String sessionId) {
        try {
            String key = "chatbot:session:" + sessionId + ":count";
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            ops.set(key, 1);
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
            
            log.debug("Reset session count for new sessionId: {} to 1", sessionId);
        } catch (Exception e) {
            log.error("Error resetting session count for sessionId {}: {}", sessionId, e.getMessage(), e);
        }
    }
    
    /**
     * Generate new session ID (UUID)
     */
    private String generateNewSessionId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Convert Message entity to MessageDTO
     */
    private MessageDTO convertMessageToDTO(Message message) {
        MessageDTO.SenderDTO senderDTO = null;
        if (message.getSender() != null) {
            senderDTO = MessageDTO.SenderDTO.builder()
                    .userId(message.getSender().getId())
                    .username(message.getSender().getUsername())
                    .fullName(message.getSender().getUsername())
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
    
    /**
     * Broadcast message to conversation participants via WebSocket
     */
    private void broadcastMessageToParticipants(Conversation conversation, MessageDTO messageDTO) {
        // Send to customer
        String customerDestination = "/user/" + conversation.getCustomer().getUsername() + "/queue/messages";
        messagingTemplate.convertAndSend(customerDestination, messageDTO);
        
        // Send to technician (chatbot user)
        String technicianDestination = "/user/" + conversation.getTechnician().getUsername() + "/queue/messages";
        messagingTemplate.convertAndSend(technicianDestination, messageDTO);
        
        log.debug("Message broadcasted to conversation participants");
    }
}

