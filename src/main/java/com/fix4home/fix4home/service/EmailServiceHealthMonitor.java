package com.fix4home.fix4home.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled health monitor for email service
 * Checks email service health periodically and logs status
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceHealthMonitor {

    private final EmailVerificationService emailVerificationService;

    /**
     * Check email service health every 30 seconds
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void checkEmailServiceHealth() {
        try {
            boolean isHealthy = emailVerificationService.checkServiceHealth();
            if (isHealthy) {
                log.debug("Email service health check: HEALTHY");
            } else {
                log.warn("Email service health check: UNHEALTHY");
            }
        } catch (Exception e) {
            log.error("Error during scheduled email service health check", e);
        }
    }

    /**
     * More frequent health check when service is unhealthy
     * Runs every 10 seconds if service was unhealthy
     */
    @Scheduled(fixedRate = 10000) // 10 seconds
    public void checkEmailServiceHealthWhenUnhealthy() {
        if (!emailVerificationService.isServiceHealthy()) {
            try {
                boolean isHealthy = emailVerificationService.checkServiceHealth();
                if (isHealthy) {
                    log.info("Email service recovered: HEALTHY");
                } else {
                    log.warn("Email service still UNHEALTHY");
                }
            } catch (Exception e) {
                log.error("Error during frequent email service health check", e);
            }
        }
    }
}

