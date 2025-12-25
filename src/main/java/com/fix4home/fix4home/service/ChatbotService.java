package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.BusinessValidationException;
import com.fix4home.fix4home.model.dto.chatbot.ChatbotHistoryDTO;
import com.fix4home.fix4home.model.dto.chatbot.ChatbotMessageResponse;
import com.fix4home.fix4home.model.entity.ChatbotMessage;
import com.fix4home.fix4home.model.entity.ChatbotSession;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.ChatbotMessageType;
import com.fix4home.fix4home.repository.ChatbotMessageRepository;
import com.fix4home.fix4home.repository.ChatbotSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Service for managing chatbot interactions with n8n webhook
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService extends BaseService {
    
    private final RestTemplate restTemplate;
    private final ChatbotSessionRepository chatbotSessionRepository;
    private final ChatbotMessageRepository chatbotMessageRepository;
    
    private static final Random RANDOM = new Random();
    private static final int MIN_SESSION_ID = 10000000; // 8 digits minimum
    private static final int MAX_SESSION_ID = 99999999; // 8 digits maximum
    
    @Value("${chatbot.n8n.webhook.url}")
    private String n8nWebhookUrl;
    
    @Value("${chatbot.n8n.timeout:30000}")
    private int timeoutMs;
    
    @Value("${chatbot.session.max-message-pairs:5}")
    private int maxMessagePairs;
    
    /**
     * Generate a unique 8-digit session ID
     */
    private String generateSessionId() {
        String sessionId;
        int attempts = 0;
        do {
            int randomNum = RANDOM.nextInt(MAX_SESSION_ID - MIN_SESSION_ID + 1) + MIN_SESSION_ID;
            sessionId = String.valueOf(randomNum);
            attempts++;
            
            if (attempts > 100) {
                throw new BusinessValidationException("Failed to generate unique session ID after 100 attempts");
            }
        } while (chatbotSessionRepository.existsBySessionId(sessionId));
        
        return sessionId;
    }
    
    /**
     * Get or create an active session for the user
     */
    @Transactional
    public ChatbotSession getOrCreateSession(Long userId) {
        User user = findUserById(userId);
        
        // Try to find active session
        return chatbotSessionRepository.findActiveSessionByUserId(userId)
                .orElseGet(() -> {
                    // Create new session
                    String sessionId = generateSessionId();
                    ChatbotSession newSession = ChatbotSession.builder()
                            .sessionId(sessionId)
                            .user(user)
                            .messageCount(0)
                            .isActive(true)
                            .build();
                    
                    ChatbotSession saved = chatbotSessionRepository.save(newSession);
                    log.info("Created new chatbot session {} for user {}", sessionId, userId);
                    return saved;
                });
    }
    
    /**
     * Check if session has reached max pairs and create new session if needed
     */
    @Transactional
    public ChatbotSession checkAndRotateSession(ChatbotSession session) {
        if (session.hasReachedMaxPairs(maxMessagePairs)) {
            log.info("Session {} reached max pairs ({}), creating new session", 
                    session.getSessionId(), maxMessagePairs);
            
            // Deactivate current session
            session.deactivate();
            chatbotSessionRepository.save(session);
            
            // Create new session
            String newSessionId = generateSessionId();
            ChatbotSession newSession = ChatbotSession.builder()
                    .sessionId(newSessionId)
                    .user(session.getUser())
                    .messageCount(0)
                    .isActive(true)
                    .build();
            
            ChatbotSession saved = chatbotSessionRepository.save(newSession);
            log.info("Created new chatbot session {} for user {}", newSessionId, session.getUser().getId());
            return saved;
        }
        return session;
    }
    
    /**
     * Call n8n webhook with message and sessionId
     */
    private String callN8nWebhook(String message, String sessionId) {
        try {
            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", message);
            requestBody.put("sessionId", sessionId);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            log.debug("Calling n8n webhook: {} with sessionId: {}", n8nWebhookUrl, sessionId);
            
            // Send request to n8n webhook
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    n8nWebhookUrl,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Object output = responseBody.get("output");
                
                if (output != null) {
                    log.info("Received response from n8n webhook for sessionId: {}", sessionId);
                    return output.toString();
                } else {
                    log.warn("N8N webhook response missing 'output' field for sessionId: {}", sessionId);
                    throw new BusinessValidationException("Invalid response from chatbot service: missing output field");
                }
            } else {
                log.warn("N8N webhook returned non-OK status: {} for sessionId: {}", 
                        response.getStatusCode(), sessionId);
                throw new BusinessValidationException("Chatbot service returned error status: " + response.getStatusCode());
            }
            
        } catch (HttpClientErrorException e) {
            log.error("Client error when calling n8n webhook for sessionId {}: {} - {}", 
                    sessionId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessValidationException("Chatbot service client error: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("Server error when calling n8n webhook for sessionId {}: {} - {}", 
                    sessionId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessValidationException("Chatbot service server error: " + e.getMessage());
        } catch (ResourceAccessException e) {
            log.error("Connection error when calling n8n webhook for sessionId {}: {}", sessionId, e.getMessage());
            throw new BusinessValidationException("Chatbot service connection error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when calling n8n webhook for sessionId {}: {}", sessionId, e.getMessage(), e);
            throw new BusinessValidationException("Chatbot service error: " + e.getMessage());
        }
    }
    
    /**
     * Send a message to the chatbot and get response
     */
    @Transactional
    public ChatbotMessageResponse sendMessage(Long userId, String userMessage) {
        validateRequired(userMessage, "message");
        validateRequired(userId, "userId");
        
        User user = findUserById(userId);
        
        // Get or create session
        ChatbotSession session = getOrCreateSession(userId);
        boolean isNewSession = false;
        
        // Check if we need to rotate session
        if (session.hasReachedMaxPairs(maxMessagePairs)) {
            session = checkAndRotateSession(session);
            isNewSession = true;
        }
        
        // Get current pair sequence (next sequence number)
        int nextPairSequence = session.getMessageCount() + 1;
        
        // Save user message
        ChatbotMessage userMsg = ChatbotMessage.builder()
                .sessionId(session.getSessionId())
                .user(user)
                .message(userMessage)
                .messageType(ChatbotMessageType.USER)
                .pairSequence(nextPairSequence)
                .build();
        chatbotMessageRepository.save(userMsg);
        log.debug("Saved user message for sessionId: {}, pairSequence: {}", session.getSessionId(), nextPairSequence);
        
        // Call n8n webhook
        String botResponse;
        try {
            botResponse = callN8nWebhook(userMessage, session.getSessionId());
            if (botResponse == null || botResponse.trim().isEmpty()) {
                log.warn("N8N webhook returned empty response for sessionId: {}", session.getSessionId());
                throw new BusinessValidationException("Chatbot service returned empty response");
            }
        } catch (BusinessValidationException e) {
            // Re-throw business exceptions
            throw e;
        } catch (Exception e) {
            log.error("Failed to get response from n8n webhook for sessionId {}: {}", session.getSessionId(), e.getMessage(), e);
            throw new BusinessValidationException("Failed to communicate with chatbot service: " + e.getMessage());
        }
        
        // Save bot response
        ChatbotMessage botMsg = ChatbotMessage.builder()
                .sessionId(session.getSessionId())
                .user(user)
                .message(botResponse)
                .messageType(ChatbotMessageType.BOT)
                .pairSequence(nextPairSequence)
                .build();
        chatbotMessageRepository.save(botMsg);
        log.debug("Saved bot response for sessionId: {}, pairSequence: {}", session.getSessionId(), nextPairSequence);
        
        // Increment message count (one pair completed)
        session.incrementMessageCount();
        chatbotSessionRepository.save(session);
        
        // Check if we need to rotate after this message
        if (session.hasReachedMaxPairs(maxMessagePairs)) {
            session = checkAndRotateSession(session);
            isNewSession = true;
        }
        
        // Build response with null safety
        String sessionIdStr = session.getSessionId();
        if (sessionIdStr == null) {
            log.error("Session ID is null for session: {}", session.getId());
            throw new BusinessValidationException("Invalid session: session ID is null");
        }
        
        return ChatbotMessageResponse.builder()
                .output(botResponse != null ? botResponse : "")
                .sessionId(sessionIdStr)
                .messageCount(session.getMessageCount() != null ? session.getMessageCount() : 0)
                .newSession(isNewSession)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Get current authenticated user ID (public method for controller access)
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
    
    /**
     * Get chat history for user with pagination
     */
    @Transactional(readOnly = true)
    public Page<ChatbotHistoryDTO> getChatHistory(Long userId, int page, int size) {
        validateRequired(userId, "userId");
        validatePaginationParams(page, size);
        
        findUserById(userId); // Validate user exists
        Pageable pageable = PageRequest.of(page, size);
        
        try {
            Page<ChatbotMessage> messages = chatbotMessageRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
            
            return messages.map(msg -> ChatbotHistoryDTO.builder()
                    .id(msg.getId() != null ? msg.getId() : 0L)
                    .sessionId(msg.getSessionId() != null ? msg.getSessionId() : "")
                    .message(msg.getMessage() != null ? msg.getMessage() : "")
                    .messageType(msg.getMessageType() != null ? msg.getMessageType() : ChatbotMessageType.USER)
                    .pairSequence(msg.getPairSequence() != null ? msg.getPairSequence() : 0)
                    .createdAt(msg.getCreatedAt() != null ? msg.getCreatedAt() : LocalDateTime.now())
                    .build());
        } catch (org.hibernate.exception.SQLGrammarException e) {
            // Table doesn't exist - return empty page
            log.warn("Chatbot tables not found, returning empty history. Migration may not have run yet.");
            return Page.empty(pageable);
        } catch (Exception e) {
            log.error("Error loading chat history for user {}: {}", userId, e.getMessage(), e);
            // Return empty page instead of throwing exception
            return Page.empty(pageable);
        }
    }
    
    /**
     * Get chat history for a specific session
     */
    @Transactional(readOnly = true)
    public List<ChatbotHistoryDTO> getSessionHistory(Long userId, String sessionId) {
        validateRequired(userId, "userId");
        validateRequired(sessionId, "sessionId");
        
        findUserById(userId); // Validate user exists
        
        try {
            // Verify session belongs to user
            ChatbotSession session = chatbotSessionRepository.findBySessionId(sessionId)
                    .orElseThrow(() -> new BusinessValidationException("Session not found: " + sessionId));
            
            if (!session.getUser().getId().equals(userId)) {
                throw new BusinessValidationException("Access denied: Session does not belong to user");
            }
            
            List<ChatbotMessage> messages = chatbotMessageRepository.findBySessionIdOrderByPairSequence(sessionId);
            
            if (messages == null || messages.isEmpty()) {
                log.info("No messages found for session: {}", sessionId);
                return new java.util.ArrayList<>();
            }
            
            return messages.stream()
                    .filter(msg -> msg != null)
                    .map(msg -> ChatbotHistoryDTO.builder()
                            .id(msg.getId() != null ? msg.getId() : 0L)
                            .sessionId(msg.getSessionId() != null ? msg.getSessionId() : "")
                            .message(msg.getMessage() != null ? msg.getMessage() : "")
                            .messageType(msg.getMessageType() != null ? msg.getMessageType() : ChatbotMessageType.USER)
                            .pairSequence(msg.getPairSequence() != null ? msg.getPairSequence() : 0)
                            .createdAt(msg.getCreatedAt() != null ? msg.getCreatedAt() : LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());
        } catch (BusinessValidationException e) {
            // Re-throw business exceptions
            throw e;
        } catch (Exception e) {
            log.error("Error loading session history for session {}: {}", sessionId, e.getMessage(), e);
            // Return empty list instead of throwing exception
            return new java.util.ArrayList<>();
        }
    }
}

