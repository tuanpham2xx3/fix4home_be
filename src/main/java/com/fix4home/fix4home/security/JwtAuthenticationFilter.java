package com.fix4home.fix4home.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                  @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // Debug logging for multipart requests
        String contentType = request.getContentType();
        String authHeader = request.getHeader("Authorization");
        boolean isMultipart = contentType != null && contentType.contains("multipart");
        
        log.debug("🔍 [SECURITY] Request: {} {}", request.getMethod(), request.getRequestURI());
        log.debug("🔍 [SECURITY] Content-Type: {}", contentType);
        log.debug("🔍 [SECURITY] Authorization header: {}", authHeader != null ? "EXISTS" : "MISSING");
        if (isMultipart) {
            log.info("🔍 [SECURITY] Multipart request detected - checking authentication");
        }
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                log.debug("🔍 [SECURITY] JWT token extracted: {}...", jwt.length() > 20 ? jwt.substring(0, 20) : jwt);
                
                if (tokenProvider.validateToken(jwt)) {
                    String username = tokenProvider.getUsernameFromToken(jwt);
                    log.debug("🔍 [SECURITY] Token validated for user: {}", username);
                    
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("🔍 [SECURITY] Authentication set in security context for user: {}", username);
                } else {
                    log.warn("⚠️ [SECURITY] Invalid JWT token");
                }
            } else {
                if (isMultipart) {
                    log.warn("⚠️ [SECURITY] Multipart request without Authorization header");
                } else {
                    log.debug("🔍 [SECURITY] No JWT token in request");
                }
            }
        } catch (Exception ex) {
            log.error("❌ [SECURITY] Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
} 