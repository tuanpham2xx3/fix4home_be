package com.fix4home.fix4home.model.enums;

public enum DevicePlatform {
    ANDROID("Android", "android", true, false),
    IOS("iOS", "ios", false, true),
    WEB("Web", "web", true, false),
    UNKNOWN("Unknown", "unknown", false, false);
    
    private final String displayName;
    private final String identifier;
    private final boolean supportsFCM;
    private final boolean supportsAPNS;
    
    DevicePlatform(String displayName, String identifier, boolean supportsFCM, boolean supportsAPNS) {
        this.displayName = displayName;
        this.identifier = identifier;
        this.supportsFCM = supportsFCM;
        this.supportsAPNS = supportsAPNS;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public boolean supportsFCM() {
        return supportsFCM;
    }
    
    public boolean supportsAPNS() {
        return supportsAPNS;
    }
    
    public static DevicePlatform fromString(String platform) {
        if (platform == null) {
            return UNKNOWN;
        }
        
        String normalizedPlatform = platform.toLowerCase().trim();
        
        return switch (normalizedPlatform) {
            case "android" -> ANDROID;
            case "ios", "iphone", "ipad" -> IOS;
            case "web", "browser", "webapp" -> WEB;
            default -> UNKNOWN;
        };
    }
    
    public static DevicePlatform fromUserAgent(String userAgent) {
        if (userAgent == null) {
            return UNKNOWN;
        }
        
        String lowerUserAgent = userAgent.toLowerCase();
        
        if (lowerUserAgent.contains("android")) {
            return ANDROID;
        } else if (lowerUserAgent.contains("iphone") || lowerUserAgent.contains("ipad") || lowerUserAgent.contains("ios")) {
            return IOS;
        } else if (lowerUserAgent.contains("mozilla") || lowerUserAgent.contains("chrome") || lowerUserAgent.contains("safari")) {
            return WEB;
        }
        
        return UNKNOWN;
    }
}
