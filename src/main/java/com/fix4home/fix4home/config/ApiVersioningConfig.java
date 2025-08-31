package com.fix4home.fix4home.config;

import com.fix4home.fix4home.security.ApiVersioningInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for API versioning strategy
 */
@Configuration
@RequiredArgsConstructor
public class ApiVersioningConfig implements WebMvcConfigurer {
    
    private final ApiVersioningInterceptor apiVersioningInterceptor;
    
    /**
     * Configure content negotiation for API versioning
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(true)
            .parameterName("version")
            .ignoreAcceptHeader(false)
            .useRegisteredExtensionsOnly(false)
            .defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .mediaType("v1", org.springframework.http.MediaType.APPLICATION_JSON)
            .mediaType("v2", org.springframework.http.MediaType.APPLICATION_JSON);
    }
    
    /**
     * Register API versioning interceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiVersioningInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/v1/auth/**", "/swagger-ui/**", "/api-docs/**");
    }
}
