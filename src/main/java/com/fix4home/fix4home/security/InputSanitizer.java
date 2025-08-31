package com.fix4home.fix4home.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Input sanitization utility for preventing XSS and injection attacks
 */
@Component
@Slf4j
public class InputSanitizer {
    
    // Patterns for potentially dangerous content
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
        "<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile(
        "javascript:", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern VBSCRIPT_PATTERN = Pattern.compile(
        "vbscript:", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern ONLOAD_PATTERN = Pattern.compile(
        "onload[^=]*=", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern ONERROR_PATTERN = Pattern.compile(
        "onerror[^=]*=", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern ONCLICK_PATTERN = Pattern.compile(
        "onclick[^=]*=", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(\\b(ALTER|CREATE|DELETE|DROP|EXEC(UTE)?|INSERT( +INTO)?|MERGE|SELECT|UPDATE|UNION( +ALL)?)\\b)", 
        Pattern.CASE_INSENSITIVE);
    
    // Removed unused HTML_TAG_PATTERN
    
    /**
     * Sanitize input text to prevent XSS attacks
     */
    public String sanitizeText(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        log.debug("Sanitizing input text");
        
        String sanitized = input
            // Remove script tags
            .replaceAll(SCRIPT_PATTERN.pattern(), "")
            // Remove javascript: protocol
            .replaceAll(JAVASCRIPT_PATTERN.pattern(), "")
            // Remove vbscript: protocol
            .replaceAll(VBSCRIPT_PATTERN.pattern(), "")
            // Remove event handlers
            .replaceAll(ONLOAD_PATTERN.pattern(), "")
            .replaceAll(ONERROR_PATTERN.pattern(), "")
            .replaceAll(ONCLICK_PATTERN.pattern(), "")
            // Escape HTML characters
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;");
        
        return sanitized.trim();
    }
    
    /**
     * Sanitize HTML content while preserving safe tags
     */
    public String sanitizeHtml(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        log.debug("Sanitizing HTML content");
        
        // Remove dangerous elements
        String sanitized = input
            .replaceAll(SCRIPT_PATTERN.pattern(), "")
            .replaceAll(JAVASCRIPT_PATTERN.pattern(), "")
            .replaceAll(VBSCRIPT_PATTERN.pattern(), "")
            .replaceAll(ONLOAD_PATTERN.pattern(), "")
            .replaceAll(ONERROR_PATTERN.pattern(), "")
            .replaceAll(ONCLICK_PATTERN.pattern(), "");
        
        return sanitized;
    }
    
    /**
     * Check for potential SQL injection patterns
     */
    public boolean containsSqlInjection(String input) {
        if (!StringUtils.hasText(input)) {
            return false;
        }
        
        boolean hasSqlInjection = SQL_INJECTION_PATTERN.matcher(input).find();
        
        if (hasSqlInjection) {
            log.warn("Potential SQL injection detected in input: {}", 
                input.length() > 50 ? input.substring(0, 50) + "..." : input);
        }
        
        return hasSqlInjection;
    }
    
    /**
     * Sanitize search query to prevent injection attacks
     */
    public String sanitizeSearchQuery(String query) {
        if (!StringUtils.hasText(query)) {
            return query;
        }
        
        log.debug("Sanitizing search query");
        
        // Remove SQL injection patterns and special characters
        String sanitized = query
            .replaceAll(SQL_INJECTION_PATTERN.pattern(), "")
            .replaceAll("[';\"\\\\]", "")
            .replaceAll("--", "")
            .replaceAll("/\\*.*?\\*/", "")
            .trim();
        
        return sanitized;
    }
    
    /**
     * Validate and sanitize file name
     */
    public String sanitizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return fileName;
        }
        
        log.debug("Sanitizing file name: {}", fileName);
        
        // Remove path traversal attempts and invalid characters
        String sanitized = fileName
            .replaceAll("[./\\\\]", "")
            .replaceAll("[<>:\"|?*]", "")
            .replaceAll("\\s+", "_")
            .trim();
        
        // Limit length
        if (sanitized.length() > 255) {
            sanitized = sanitized.substring(0, 255);
        }
        
        return sanitized;
    }
    
    /**
     * Validate if input contains only safe characters
     */
    public boolean isSafeInput(String input) {
        if (!StringUtils.hasText(input)) {
            return true;
        }
        
        return !SCRIPT_PATTERN.matcher(input).find() &&
               !JAVASCRIPT_PATTERN.matcher(input).find() &&
               !VBSCRIPT_PATTERN.matcher(input).find() &&
               !containsSqlInjection(input);
    }
    
    /**
     * Sanitize phone number to only contain digits and basic formatting
     */
    public String sanitizePhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return phoneNumber;
        }
        
        // Keep only digits, spaces, dashes, parentheses, and plus sign
        return phoneNumber.replaceAll("[^0-9\\s\\-\\(\\)\\+]", "").trim();
    }
    
    /**
     * Sanitize email to prevent injection while preserving format
     */
    public String sanitizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return email;
        }
        
        // Remove dangerous characters while keeping valid email characters
        return email.replaceAll("[<>\"'\\\\;]", "").trim();
    }
}
