package com.fix4home.fix4home.config;

import com.fix4home.fix4home.security.JwtChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time chat functionality
 * Configures STOMP messaging with SockJS fallback and JWT authentication
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private final JwtChannelInterceptor jwtChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for pub/sub messaging
        config.enableSimpleBroker("/topic", "/queue", "/user");
        
        // Set application destination prefix for messages from client
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint for WebSocket connection
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Configure based on your frontend URLs
                .withSockJS(); // Enable SockJS fallback for browsers without WebSocket support
        
        // Additional endpoint without SockJS for native WebSocket clients
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add JWT authentication interceptor for WebSocket connections
        registration.interceptors(jwtChannelInterceptor);
    }
} 