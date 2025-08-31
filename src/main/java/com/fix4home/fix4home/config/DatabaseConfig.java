package com.fix4home.fix4home.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Database Configuration
 * Provides advanced database connection pool configuration and monitoring
 */
@Configuration
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
@Slf4j
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.hikari.pool-name:Fix4HomeHikariPool}")
    private String poolName;

    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.idle-timeout:300000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1200000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.leak-detection-threshold:60000}")
    private long leakDetectionThreshold;

    @Value("${spring.datasource.hikari.validation-timeout:5000}")
    private long validationTimeout;

    @Value("${spring.datasource.hikari.connection-test-query:SELECT 1}")
    private String connectionTestQuery;

    /**
     * Primary DataSource configuration with HikariCP
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Basic connection settings
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Pool settings
        config.setPoolName(poolName);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setConnectionTimeout(connectionTimeout);
        config.setLeakDetectionThreshold(leakDetectionThreshold);
        config.setValidationTimeout(validationTimeout);
        config.setConnectionTestQuery(connectionTestQuery);
        
        // Performance optimizations
        config.setAutoCommit(false); // Let Spring manage transactions
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        config.setIsolateInternalQueries(true);
        config.setAllowPoolSuspension(true);
        config.setReadOnly(false);
        config.setRegisterMbeans(true); // Enable JMX monitoring
        
        // Connection properties for MySQL optimization
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("useLocalSessionState", true);
        config.addDataSourceProperty("rewriteBatchedStatements", true);
        config.addDataSourceProperty("cacheResultSetMetadata", true);
        config.addDataSourceProperty("cacheServerConfiguration", true);
        config.addDataSourceProperty("elideSetAutoCommits", true);
        config.addDataSourceProperty("maintainTimeStats", false);
        config.addDataSourceProperty("useSSL", false);
        config.addDataSourceProperty("allowPublicKeyRetrieval", true);
        config.addDataSourceProperty("useUnicode", true);
        config.addDataSourceProperty("characterEncoding", "UTF-8");
        config.addDataSourceProperty("serverTimezone", "UTC");
        
        // Create and configure the datasource
        HikariDataSource dataSource = new HikariDataSource(config);
        
        log.info("HikariCP DataSource configured:");
        log.info("  Pool Name: {}", poolName);
        log.info("  Maximum Pool Size: {}", maximumPoolSize);
        log.info("  Minimum Idle: {}", minimumIdle);
        log.info("  Connection Timeout: {}ms", connectionTimeout);
        log.info("  Max Lifetime: {}ms", maxLifetime);
        log.info("  Leak Detection Threshold: {}ms", leakDetectionThreshold);
        
        return dataSource;
    }

    /**
     * Read-only DataSource for reporting queries (if needed)
     */
    @Bean("readOnlyDataSource")
    @Profile("!test")
    public DataSource readOnlyDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Use the same connection settings but mark as read-only
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Smaller pool for read-only operations
        config.setPoolName("Fix4HomeReadOnlyPool");
        config.setMaximumPoolSize(Math.max(2, maximumPoolSize / 4));
        config.setMinimumIdle(1);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setConnectionTimeout(connectionTimeout);
        config.setValidationTimeout(validationTimeout);
        config.setConnectionTestQuery(connectionTestQuery);
        
        // Read-only optimizations
        config.setReadOnly(true);
        config.setAutoCommit(true);
        config.setTransactionIsolation("TRANSACTION_READ_UNCOMMITTED");
        config.setRegisterMbeans(true);
        
        // Same MySQL optimizations
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 150);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("useLocalSessionState", true);
        config.addDataSourceProperty("cacheResultSetMetadata", true);
        config.addDataSourceProperty("cacheServerConfiguration", true);
        config.addDataSourceProperty("maintainTimeStats", false);
        config.addDataSourceProperty("useSSL", false);
        config.addDataSourceProperty("allowPublicKeyRetrieval", true);
        config.addDataSourceProperty("useUnicode", true);
        config.addDataSourceProperty("characterEncoding", "UTF-8");
        config.addDataSourceProperty("serverTimezone", "UTC");
        
        log.info("Read-only DataSource configured with pool size: {}", config.getMaximumPoolSize());
        
        return new HikariDataSource(config);
    }

    /**
     * Database Health Check Configuration
     */
    @Bean
    public DatabaseHealthIndicator databaseHealthIndicator(DataSource dataSource) {
        return new DatabaseHealthIndicator(dataSource);
    }

    /**
     * Custom Database Health Indicator
     */
    public static class DatabaseHealthIndicator {
        private final DataSource dataSource;

        public DatabaseHealthIndicator(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public boolean isHealthy() {
            try {
                if (dataSource instanceof HikariDataSource) {
                    HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                    return hikariDataSource.getHikariPoolMXBean().getActiveConnections() >= 0;
                }
                return dataSource.getConnection().isValid(5);
            } catch (Exception e) {
                log.error("Database health check failed", e);
                return false;
            }
        }

        public int getActiveConnections() {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                return hikariDataSource.getHikariPoolMXBean().getActiveConnections();
            }
            return -1;
        }

        public int getIdleConnections() {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                return hikariDataSource.getHikariPoolMXBean().getIdleConnections();
            }
            return -1;
        }

        public int getTotalConnections() {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                return hikariDataSource.getHikariPoolMXBean().getTotalConnections();
            }
            return -1;
        }

        public DataSource getDataSource() {
            return dataSource;
        }
    }
}
