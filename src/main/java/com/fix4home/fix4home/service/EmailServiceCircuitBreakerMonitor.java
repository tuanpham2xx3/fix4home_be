package com.fix4home.fix4home.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Monitor circuit breaker state for email service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceCircuitBreakerMonitor {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * Log circuit breaker state every 30 seconds
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void logCircuitBreakerState() {
        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("emailService");
            if (circuitBreaker != null) {
                CircuitBreaker.State state = circuitBreaker.getState();
                CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
                
                log.info("Email service circuit breaker state: {}, " +
                        "Failure rate: {}%, " +
                        "Number of successful calls: {}, " +
                        "Number of failed calls: {}, " +
                        "Number of not permitted calls: {}",
                        state,
                        metrics.getFailureRate(),
                        metrics.getNumberOfSuccessfulCalls(),
                        metrics.getNumberOfFailedCalls(),
                        metrics.getNumberOfNotPermittedCalls());
                
                if (state == CircuitBreaker.State.OPEN) {
                    log.warn("⚠️ CIRCUIT BREAKER IS OPEN - Email service requests will be blocked!");
                }
            }
        } catch (Exception e) {
            log.error("Error checking circuit breaker state", e);
        }
    }
}

