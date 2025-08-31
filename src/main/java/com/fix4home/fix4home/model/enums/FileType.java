package com.fix4home.fix4home.model.enums;

import java.util.Arrays;
import java.util.List;

public enum FileType {
    IMAGE("image", Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp")),
    DOCUMENT("document", Arrays.asList("pdf", "doc", "docx", "txt", "rtf", "xls", "xlsx", "ppt", "pptx")),
    VIDEO("video", Arrays.asList("mp4", "avi", "mov", "wmv", "flv", "webm")),
    AUDIO("audio", Arrays.asList("mp3", "wav", "aac", "flac", "ogg")),
    OTHER("other", Arrays.asList());
    
    private final String category;
    private final List<String> extensions;
    
    FileType(String category, List<String> extensions) {
        this.category = category;
        this.extensions = extensions;
    }
    
    public String getCategory() {
        return category;
    }
    
    public List<String> getExtensions() {
        return extensions;
    }
    
    public static FileType fromExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return OTHER;
        }
        
        String normalizedExtension = extension.toLowerCase();
        if (normalizedExtension.startsWith(".")) {
            normalizedExtension = normalizedExtension.substring(1);
        }
        
        for (FileType type : values()) {
            if (type.getExtensions().contains(normalizedExtension)) {
                return type;
            }
        }
        
        return OTHER;
    }
    
    public static FileType fromContentType(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return OTHER;
        }
        
        String lowerContentType = contentType.toLowerCase();
        
        if (lowerContentType.startsWith("image/")) {
            return IMAGE;
        } else if (lowerContentType.startsWith("video/")) {
            return VIDEO;
        } else if (lowerContentType.startsWith("audio/")) {
            return AUDIO;
        } else if (lowerContentType.contains("pdf") || 
                   lowerContentType.contains("document") ||
                   lowerContentType.contains("spreadsheet") ||
                   lowerContentType.contains("presentation") ||
                   lowerContentType.contains("text/")) {
            return DOCUMENT;
        }
        
        return OTHER;
    }
    
    public boolean isImageType() {
        return this == IMAGE;
    }
    
    public boolean isDocumentType() {
        return this == DOCUMENT;
    }
    
    public boolean isVideoType() {
        return this == VIDEO;
    }
    
    public boolean isAudioType() {
        return this == AUDIO;
    }
}
