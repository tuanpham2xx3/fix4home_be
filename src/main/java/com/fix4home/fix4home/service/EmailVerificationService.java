package com.fix4home.fix4home.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final RestTemplate restTemplate;

    @Value("${email.verification.service.url:http://localhost:8200}")
    private String emailServiceUrl;

    @Value("${email.verification.service.api-key}")
    private String apiKey;

    @Value("${email.verification.system.name:Fix4Home}")
    private String systemName;

    /**
     * Send verification code to email
     * @param email recipient email
     * @param action action type (registration, forgot_password, etc.)
     * @param userId user ID for tracking
     * @return true if email sent successfully
     */
    public boolean sendVerificationCode(String email, String action, Long userId) {
        try {
            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("system", systemName);
            
            Map<String, Object> customData = new HashMap<>();
            customData.put("user_id", userId.toString());
            customData.put("action", action);
            requestBody.put("customData", customData);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Send request to microservice
            ResponseEntity<Map> response = restTemplate.postForEntity(
                emailServiceUrl + "/generate", 
                request, 
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Verification code sent successfully to email: {} for action: {}", email, action);
                return true;
            } else {
                log.warn("Failed to send verification code. Status: {}", response.getStatusCode());
                return false;
            }

        } catch (HttpClientErrorException e) {
            log.error("Client error when sending verification code to {}: {} - {}", 
                email, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (HttpServerErrorException e) {
            log.error("Server error when sending verification code to {}: {} - {}", 
                email, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (ResourceAccessException e) {
            log.error("Connection error when sending verification code to {}: {}", email, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error when sending verification code to {}: {}", email, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verify the code sent to email
     * @param email email address
     * @param code verification code
     * @return true if code is valid
     */
    public boolean verifyCode(String email, String code) {
        try {
            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("code", code);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Send request to microservice
            ResponseEntity<Map> response = restTemplate.postForEntity(
                emailServiceUrl + "/verify", 
                request, 
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Verification code verified successfully for email: {}", email);
                return true;
            } else {
                log.warn("Failed to verify code for email: {}. Status: {}", email, response.getStatusCode());
                return false;
            }

        } catch (HttpClientErrorException e) {
            log.error("Client error when verifying code for {}: {} - {}", 
                email, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (HttpServerErrorException e) {
            log.error("Server error when verifying code for {}: {} - {}", 
                email, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (ResourceAccessException e) {
            log.error("Connection error when verifying code for {}: {}", email, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error when verifying code for {}: {}", email, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check health of email verification service
     * @return true if service is healthy
     */
    public boolean checkServiceHealth() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                emailServiceUrl + "/health", 
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();
                return body != null && "healthy".equals(body.get("status"));
            }
            return false;

        } catch (Exception e) {
            log.error("Error checking email service health: {}", e.getMessage());
            return false;
        }
    }
} 