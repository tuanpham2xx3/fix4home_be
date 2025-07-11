package com.fix4home.fix4home.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * JWT authentication interceptor for WebSocket connections
 * Validates JWT tokens and sets user context for WebSocket messages
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Extract JWT token from headers
            String token = accessor.getFirstNativeHeader("Authorization");
            
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    String jwt = token.substring(7);
                    
                    // Validate token
                    if (jwtTokenProvider.validateToken(jwt)) {
                        String username = jwtTokenProvider.getUsernameFromToken(jwt);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        
                        // Create authentication object
                        UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        
                        // Set user context for WebSocket session
                        accessor.setUser(authentication);
                        
                        log.debug("WebSocket connection authenticated for user: {}", username);
                    } else {
                        log.warn("Invalid JWT token in WebSocket connection");
                        throw new SecurityException("Invalid token");
                    }
                } catch (Exception e) {
                    log.error("Error authenticating WebSocket connection: {}", e.getMessage());
                    throw new SecurityException("Authentication failed", e);
                }
            } else {
                log.warn("No Authorization header found in WebSocket connection");
                throw new SecurityException("No authorization token provided");
            }
        }
        
        return message;
    }
} 