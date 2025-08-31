package com.fix4home.fix4home.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis and Cache Configuration
 * Provides configuration for Redis connection, caching, and serialization
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    @Value("${cache.default.ttl:3600}")
    private long defaultCacheTtl; // Default 1 hour

    // ==================== REDIS CONNECTION ====================

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        log.info("Configuring Redis connection to {}:{}", redisHost, redisPort);
        
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setDatabase(redisDatabase);
        
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }

        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure JSON serialization
        Jackson2JsonRedisSerializer<Object> jsonSerializer = createJsonSerializer();

        // Set serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.setDefaultSerializer(jsonSerializer);
        template.afterPropertiesSet();

        log.info("Redis template configured successfully");
        return template;
    }

    // ==================== CACHE MANAGER ====================

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("Configuring Redis cache manager with default TTL: {} seconds", defaultCacheTtl);

        RedisCacheConfiguration defaultConfig = createCacheConfiguration(Duration.ofSeconds(defaultCacheTtl));
        
        // Configure specific cache TTLs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // User profile caches - longer TTL since profile data changes less frequently
        cacheConfigurations.put("userProfiles", createCacheConfiguration(Duration.ofHours(2)));
        cacheConfigurations.put("customerProfiles", createCacheConfiguration(Duration.ofHours(2)));
        cacheConfigurations.put("technicianProfiles", createCacheConfiguration(Duration.ofHours(2)));
        
        // Service data - medium TTL
        cacheConfigurations.put("services", createCacheConfiguration(Duration.ofMinutes(30)));
        cacheConfigurations.put("servicePosts", createCacheConfiguration(Duration.ofMinutes(15)));
        cacheConfigurations.put("serviceRequests", createCacheConfiguration(Duration.ofMinutes(10)));
        
        // Address data - longer TTL since it's relatively static
        cacheConfigurations.put("addresses", createCacheConfiguration(Duration.ofHours(6)));
        cacheConfigurations.put("provinces", createCacheConfiguration(Duration.ofHours(24)));
        cacheConfigurations.put("wards", createCacheConfiguration(Duration.ofHours(24)));
        
        // Statistics and metrics - short TTL for real-time data
        cacheConfigurations.put("statistics", createCacheConfiguration(Duration.ofMinutes(5)));
        cacheConfigurations.put("dashboardStats", createCacheConfiguration(Duration.ofMinutes(2)));
        
        // Feedback and ratings - medium TTL
        cacheConfigurations.put("feedbacks", createCacheConfiguration(Duration.ofMinutes(30)));
        cacheConfigurations.put("ratings", createCacheConfiguration(Duration.ofMinutes(30)));
        
        // Chat and notifications - short TTL for real-time features
        cacheConfigurations.put("conversations", createCacheConfiguration(Duration.ofMinutes(5)));
        cacheConfigurations.put("notifications", createCacheConfiguration(Duration.ofMinutes(2)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    // ==================== HELPER METHODS ====================

    private RedisCacheConfiguration createCacheConfiguration(Duration ttl) {
        Jackson2JsonRedisSerializer<Object> jsonSerializer = createJsonSerializer();
        
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();
    }

    private Jackson2JsonRedisSerializer<Object> createJsonSerializer() {
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        
        // Use constructor to avoid deprecated setObjectMapper method
        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }
}
