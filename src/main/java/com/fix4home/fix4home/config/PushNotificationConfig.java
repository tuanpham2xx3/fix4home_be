package com.fix4home.fix4home.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "push.notification")
@Data
public class PushNotificationConfig {
    
    private boolean enabled = true;
    private int batchSize = 100;
    private int retryAttempts = 3;
    private int retryDelaySeconds = 5;
    
    private Fcm fcm = new Fcm();
    private Apns apns = new Apns();
    
    @Data
    public static class Fcm {
        private String serviceAccountKey;
        private String projectId;
    }
    
    @Data
    public static class Apns {
        private String certificatePath;
        private String certificatePassword;
        private boolean production = false;
    }
}
