package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.UserNotFoundException;
import com.fix4home.fix4home.model.entity.Conversation;
import com.fix4home.fix4home.model.entity.Message;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.ConversationType;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.repository.ConversationRepository;
import com.fix4home.fix4home.repository.MessageRepository;
import com.fix4home.fix4home.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

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
    
    @Value("${chatbot.enabled:true}")
    private boolean chatbotEnabled;
    
    @Value("${chatbot.user.username:chatbot_support}")
    private String chatbotUsername;
    
    @Value("${chatbot.n8n.webhook.url:}")
    private String n8nWebhookUrl;
    
    @Value("${chatbot.n8n.timeout:30000}")
    private int n8nTimeout;
    
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
}

