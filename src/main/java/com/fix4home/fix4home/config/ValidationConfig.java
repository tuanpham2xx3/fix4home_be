package com.fix4home.fix4home.config;

import com.fix4home.fix4home.security.InputSanitizer;
import com.fix4home.fix4home.security.ValidationAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration for input validation and sanitization
 */
@Configuration
@EnableAspectJAutoProxy
public class ValidationConfig {
    
    @Bean
    public InputSanitizer inputSanitizer() {
        return new InputSanitizer();
    }
    
    @Bean
    public ValidationAspect validationAspect(InputSanitizer inputSanitizer) {
        return new ValidationAspect(inputSanitizer);
    }
}


