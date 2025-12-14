package com.fix4home.fix4home.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;

/**
 * Configuration for multipart file upload handling.
 * 
 * This configuration ensures that multipart requests are resolved lazily,
 * allowing Spring Security filters to process authentication before the
 * multipart body is parsed. This is critical for JWT authentication with
 * multipart/form-data requests.
 */
@Configuration
public class MultipartConfig {

    /**
     * Configure multipart resolver with lazy resolution.
     * 
     * Lazy resolution ensures that:
     * 1. Spring Security filters (including JwtAuthenticationFilter) run first
     * 2. Authorization header is processed before multipart body parsing
     * 3. Authentication is set in SecurityContext before controller method execution
     */
    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        // Don't resolve multipart eagerly - let security filters run first
        resolver.setResolveLazily(true);
        return resolver;
    }

    /**
     * Configure multipart file size limits.
     * 
     * These limits are enforced at the servlet container level.
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Max file size: 10MB (matches application.properties)
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        
        // Max request size: 50MB (matches application.properties)
        factory.setMaxRequestSize(DataSize.ofMegabytes(50));
        
        // File size threshold: 2KB (files smaller than this are kept in memory)
        factory.setFileSizeThreshold(DataSize.ofKilobytes(2));
        
        return factory.createMultipartConfig();
    }
}

