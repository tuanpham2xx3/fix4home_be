package com.fix4home.fix4home.config;

import com.fix4home.fix4home.security.AuditLoggingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration for audit logging
 */
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AuditConfig {
    
    private final AuditLoggingFilter auditLoggingFilter;
    
    /**
     * Register audit logging filter
     */
    @Bean
    public FilterRegistrationBean<AuditLoggingFilter> auditLoggingFilterRegistration() {
        FilterRegistrationBean<AuditLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(auditLoggingFilter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(2); // After security filter
        registration.setName("auditLoggingFilter");
        return registration;
    }
}
