package com.fix4home.fix4home.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "file.storage")
@Data
public class FileStorageConfig {
    
    private String type = "local"; // local, s3
    private List<String> allowedExtensions;
    private Long maxFileSize;
    
    private Local local = new Local();
    private Image image = new Image();
    
    @Data
    public static class Local {
        private String uploadDir = "uploads/";
        private String baseUrl = "http://localhost:8100/api/v1/files/";
    }
    
    @Data
    public static class Image {
        private Integer maxWidth = 2048;
        private Integer maxHeight = 2048;
        private Thumbnail thumbnail = new Thumbnail();
        
        @Data
        public static class Thumbnail {
            private Integer width = 300;
            private Integer height = 300;
        }
    }
}
