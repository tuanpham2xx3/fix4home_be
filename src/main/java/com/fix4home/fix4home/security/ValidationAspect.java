package com.fix4home.fix4home.security;

import com.fix4home.fix4home.exception.SecurityValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * AOP aspect for automatic input validation and sanitization
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationAspect {
    
    private final InputSanitizer inputSanitizer;
    
    /**
     * Validate inputs for all controller methods
     */
    @Before("execution(* com.fix4home.fix4home.controller..*(..))")
    public void validateControllerInputs(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        
        if (args != null && args.length > 0) {
            log.debug("Validating inputs for method: {}", joinPoint.getSignature().getName());
            
            for (Object arg : args) {
                if (arg != null) {
                    validateObject(arg);
                }
            }
        }
    }
    
    /**
     * Recursively validate an object and its fields
     */
    private void validateObject(Object obj) {
        if (obj == null) {
            return;
        }
        
        // Skip primitive types and their wrappers
        if (isPrimitiveOrWrapper(obj.getClass())) {
            if (obj instanceof String) {
                validateStringInput((String) obj);
            }
            return;
        }
        
        // Skip collections validation for now (can be enhanced later)
        if (obj instanceof Collection) {
            return;
        }
        
        // Validate object fields
        Class<?> clazz = obj.getClass();
        
        // Skip Spring/framework classes
        if (clazz.getName().startsWith("org.springframework") ||
            clazz.getName().startsWith("java.") ||
            clazz.getName().startsWith("javax.")) {
            return;
        }
        
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(obj);
                
                if (fieldValue instanceof String) {
                    validateStringInput((String) fieldValue);
                } else if (fieldValue != null && !isPrimitiveOrWrapper(fieldValue.getClass())) {
                    // Recursively validate nested objects (with depth limit)
                    validateObject(fieldValue);
                }
            } catch (IllegalAccessException e) {
                log.debug("Could not access field {} for validation", field.getName());
            }
        }
    }
    
    /**
     * Validate string input for security threats
     */
    private void validateStringInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        
        // Check for SQL injection
        if (inputSanitizer.containsSqlInjection(input)) {
            log.warn("SQL injection attempt detected: {}", 
                input.length() > 100 ? input.substring(0, 100) + "..." : input);
            throw new SecurityValidationException("Invalid input detected: potential SQL injection");
        }
        
        // Check for XSS attempts
        if (!inputSanitizer.isSafeInput(input)) {
            log.warn("XSS attempt detected: {}", 
                input.length() > 100 ? input.substring(0, 100) + "..." : input);
            throw new SecurityValidationException("Invalid input detected: potential XSS attack");
        }
        
        // Check for excessively long inputs (DoS protection)
        if (input.length() > 10000) {
            log.warn("Excessively long input detected: {} characters", input.length());
            throw new SecurityValidationException("Input too long: maximum 10,000 characters allowed");
        }
    }
    
    /**
     * Check if a class is a primitive type or wrapper
     */
    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
               clazz == Boolean.class ||
               clazz == Byte.class ||
               clazz == Character.class ||
               clazz == Double.class ||
               clazz == Float.class ||
               clazz == Integer.class ||
               clazz == Long.class ||
               clazz == Short.class ||
               clazz == String.class;
    }
}
