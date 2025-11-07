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
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Value("${email.verification.method:verification_code}")
    private String verificationMethod; // "verification_code" or "activation_link"

    @Value("${email.activation.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

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

    /**
     * Send password reset email with temporary password
     * @param email recipient email
     * @param tempPassword temporary password to include in email
     * @param userId user ID for tracking
     * @return activation response with resend info
     */
    public ActivationResponse sendPasswordResetWithTempPassword(String email, String tempPassword, Long userId) {
        try {
            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            // Prepare request body with temp password in custom data
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("action", "password_reset");
            requestBody.put("system", systemName);
            requestBody.put("baseUrl", frontendBaseUrl);
            
            Map<String, Object> customData = new HashMap<>();
            customData.put("user_id", userId.toString());
            customData.put("action", "password_reset");
            customData.put("temp_password", tempPassword); // Include temp password in email
            requestBody.put("customData", customData);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Send request to microservice
            ResponseEntity<Map> response = restTemplate.postForEntity(
                emailServiceUrl + "/generate-activation", 
                request, 
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                log.info("Password reset email with temp password sent successfully to: {}", email);
                
                return ActivationResponse.builder()
                    .success(true)
                    .message("Password reset email sent successfully")
                    .canResend((Boolean) responseBody.get("can_resend"))
                    .nextResendAt((Long) responseBody.get("next_resend_at"))
                    .sendCount((Integer) responseBody.get("send_count"))
                    .maxSends((Integer) responseBody.get("max_sends"))
                    .build();
            } else {
                log.warn("Failed to send password reset email. Status: {}", response.getStatusCode());
                return ActivationResponse.builder()
                    .success(false)
                    .message("Failed to send password reset email")
                    .build();
            }

        } catch (Exception e) {
            log.error("Unexpected error when sending password reset email to {}: {}", email, e.getMessage(), e);
            return ActivationResponse.builder()
                .success(false)
                .message("Failed to send password reset email")
                .build();
        }
    }

    /**
     * Send activation link to email (new method)
     * @param email recipient email
     * @param action action type (registration, password_reset, etc.)
     * @param userId user ID for tracking
     * @return activation response with resend info
     */
    public ActivationResponse sendActivationLink(String email, String action, Long userId) {
        try {
            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("action", action);
            requestBody.put("system", systemName);
            requestBody.put("baseUrl", frontendBaseUrl);
            
            Map<String, Object> customData = new HashMap<>();
            customData.put("user_id", userId.toString());
            customData.put("action", action);
            requestBody.put("customData", customData);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Send request to microservice
            ResponseEntity<Map> response = restTemplate.postForEntity(
                emailServiceUrl + "/generate-activation", 
                request, 
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                log.info("Activation link sent successfully to email: {} for action: {}", email, action);
                
                return ActivationResponse.builder()
                    .success(true)
                    .message("Activation link sent successfully")
                    .canResend((Boolean) responseBody.get("can_resend"))
                    .nextResendAt((Long) responseBody.get("next_resend_at"))
                    .sendCount((Integer) responseBody.get("send_count"))
                    .maxSends((Integer) responseBody.get("max_sends"))
                    .build();
            } else {
                log.warn("Failed to send activation link. Status: {}", response.getStatusCode());
                return ActivationResponse.builder()
                    .success(false)
                    .message("Failed to send activation link")
                    .build();
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                // Parse rate limit response
                try {
                    Map<String, Object> errorBody = new ObjectMapper().readValue(e.getResponseBodyAsString(), Map.class);
                    return ActivationResponse.builder()
                        .success(false)
                        .message((String) errorBody.get("message"))
                        .canResend((Boolean) errorBody.get("can_resend"))
                        .nextResendAt((Long) errorBody.get("next_resend_at"))
                        .sendCount((Integer) errorBody.get("send_count"))
                        .maxSends((Integer) errorBody.get("max_sends"))
                        .build();
                } catch (Exception parseException) {
                    log.error("Error parsing rate limit response", parseException);
                }
            }
            
            log.error("Client error when sending activation link to {}: {} - {}", 
                email, e.getStatusCode(), e.getResponseBodyAsString());
            return ActivationResponse.builder()
                .success(false)
                .message("Failed to send activation link")
                .build();
        } catch (Exception e) {
            log.error("Unexpected error when sending activation link to {}: {}", email, e.getMessage(), e);
            return ActivationResponse.builder()
                .success(false)
                .message("Failed to send activation link")
                .build();
        }
    }

    /**
     * Verify activation token
     * @param token activation token from email link
     * @return activation data if valid
     */
    public ActivationData verifyActivationToken(String token) {
        try {
            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("token", token);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Send request to microservice
            ResponseEntity<Map> response = restTemplate.postForEntity(
                emailServiceUrl + "/verify-activation", 
                request, 
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                
                log.info("Activation token verified successfully for email: {}", data.get("email"));
                
                return ActivationData.builder()
                    .email((String) data.get("email"))
                    .action((String) data.get("action"))
                    .system((String) data.get("system"))
                    .build();
            } else {
                log.warn("Failed to verify activation token. Status: {}", response.getStatusCode());
                return null;
            }

        } catch (HttpClientErrorException e) {
            log.error("Client error when verifying activation token: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error when verifying activation token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Resend activation email
     * @param email recipient email
     * @param action action type
     * @return activation response with resend info
     */
    public ActivationResponse resendActivationLink(String email, String action) {
        try {
            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("action", action);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Send request to microservice
            ResponseEntity<Map> response = restTemplate.postForEntity(
                emailServiceUrl + "/resend-activation", 
                request, 
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                log.info("Activation link resent successfully to email: {} for action: {}", email, action);
                
                return ActivationResponse.builder()
                    .success(true)
                    .message("Activation link resent successfully")
                    .canResend((Boolean) responseBody.get("can_resend"))
                    .nextResendAt((Long) responseBody.get("next_resend_at"))
                    .sendCount((Integer) responseBody.get("send_count"))
                    .maxSends((Integer) responseBody.get("max_sends"))
                    .build();
            } else {
                log.warn("Failed to resend activation link. Status: {}", response.getStatusCode());
                return ActivationResponse.builder()
                    .success(false)
                    .message("Failed to resend activation link")
                    .build();
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                // Parse rate limit response
                try {
                    Map<String, Object> errorBody = new ObjectMapper().readValue(e.getResponseBodyAsString(), Map.class);
                    return ActivationResponse.builder()
                        .success(false)
                        .message((String) errorBody.get("message"))
                        .canResend((Boolean) errorBody.get("can_resend"))
                        .nextResendAt((Long) errorBody.get("next_resend_at"))
                        .sendCount((Integer) errorBody.get("send_count"))
                        .maxSends((Integer) errorBody.get("max_sends"))
                        .build();
                } catch (Exception parseException) {
                    log.error("Error parsing rate limit response", parseException);
                }
            }
            
            log.error("Client error when resending activation link to {}: {} - {}", 
                email, e.getStatusCode(), e.getResponseBodyAsString());
            return ActivationResponse.builder()
                .success(false)
                .message("Failed to resend activation link")
                .build();
        } catch (Exception e) {
            log.error("Unexpected error when resending activation link to {}: {}", email, e.getMessage(), e);
            return ActivationResponse.builder()
                .success(false)
                .message("Failed to resend activation link")
                .build();
        }
    }

    /**
     * Send verification based on configured method (code or link)
     * @param email recipient email
     * @param action action type
     * @param userId user ID
     * @return true if sent successfully (for backward compatibility)
     */
    public boolean sendVerification(String email, String action, Long userId) {
        if ("activation_link".equals(verificationMethod)) {
            ActivationResponse response = sendActivationLink(email, action, userId);
            return response.isSuccess();
        } else {
            return sendVerificationCode(email, action, userId);
        }
    }

    // Inner classes for response DTOs
    @lombok.Data
    @lombok.Builder
    public static class ActivationResponse {
        private boolean success;
        private String message;
        private boolean canResend;
        private Long nextResendAt;
        private Integer sendCount;
        private Integer maxSends;
    }

    @lombok.Data
    @lombok.Builder
    public static class ActivationData {
        private String email;
        private String action;
        private String system;
    }
} 