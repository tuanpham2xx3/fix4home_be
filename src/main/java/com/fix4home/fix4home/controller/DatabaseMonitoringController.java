package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.config.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Database Monitoring Controller
 * Provides endpoints for monitoring database health and performance
 */
@RestController
@RequestMapping("/api/v1/admin/database/monitoring")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Database Monitoring", description = "Database health and performance monitoring")
@SecurityRequirement(name = "bearerAuth")
public class DatabaseMonitoringController implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DatabaseConfig.DatabaseHealthIndicator databaseHealthIndicator;

    /**
     * Get comprehensive database health status
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get database health", description = "Get comprehensive database health status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Health status retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public ResponseEntity<Map<String, Object>> getDatabaseHealth() {
        log.info("Getting database health status");
        
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Basic connectivity test
            boolean isHealthy = databaseHealthIndicator.isHealthy();
            health.put("status", isHealthy ? "UP" : "DOWN");
            
            // Connection pool statistics
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
                
                Map<String, Object> connectionPool = new HashMap<>();
                connectionPool.put("activeConnections", poolBean.getActiveConnections());
                connectionPool.put("idleConnections", poolBean.getIdleConnections());
                connectionPool.put("totalConnections", poolBean.getTotalConnections());
                connectionPool.put("threadsAwaitingConnection", poolBean.getThreadsAwaitingConnection());
                connectionPool.put("maximumPoolSize", hikariDataSource.getMaximumPoolSize());
                connectionPool.put("minimumIdle", hikariDataSource.getMinimumIdle());
                
                health.put("connectionPool", connectionPool);
            }
            
            // Database statistics
            Map<String, Object> dbStats = getDatabaseStatistics();
            health.put("databaseStatistics", dbStats);
            
            health.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("Error getting database health", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(health);
    }

    /**
     * Get connection pool statistics
     */
    @GetMapping("/connection-pool")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get connection pool stats", description = "Get detailed connection pool statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Connection pool stats retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public ResponseEntity<Map<String, Object>> getConnectionPoolStats() {
        log.info("Getting connection pool statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        try {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
                
                stats.put("poolName", hikariDataSource.getPoolName());
                stats.put("activeConnections", poolBean.getActiveConnections());
                stats.put("idleConnections", poolBean.getIdleConnections());
                stats.put("totalConnections", poolBean.getTotalConnections());
                stats.put("threadsAwaitingConnection", poolBean.getThreadsAwaitingConnection());
                stats.put("maximumPoolSize", hikariDataSource.getMaximumPoolSize());
                stats.put("minimumIdle", hikariDataSource.getMinimumIdle());
                stats.put("connectionTimeout", hikariDataSource.getConnectionTimeout());
                stats.put("idleTimeout", hikariDataSource.getIdleTimeout());
                stats.put("maxLifetime", hikariDataSource.getMaxLifetime());
                stats.put("leakDetectionThreshold", hikariDataSource.getLeakDetectionThreshold());
                
                // Calculate utilization percentages
                int active = poolBean.getActiveConnections();
                int max = hikariDataSource.getMaximumPoolSize();
                double utilization = max > 0 ? (double) active / max * 100 : 0;
                stats.put("poolUtilizationPercent", Math.round(utilization * 100.0) / 100.0);
                
                // Connection health
                stats.put("healthy", active < max && poolBean.getThreadsAwaitingConnection() == 0);
                
            } else {
                stats.put("error", "DataSource is not HikariDataSource");
            }
            
            stats.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("Error getting connection pool stats", e);
            stats.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Get database performance statistics
     */
    @GetMapping("/performance")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get performance stats", description = "Get database performance statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Performance stats retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public ResponseEntity<Map<String, Object>> getPerformanceStats() {
        log.info("Getting database performance statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Get database size and table statistics
            stats.putAll(getDatabaseStatistics());
            
            // Get active processes
            stats.put("activeProcesses", getActiveProcessCount());
            
            // Get slow query information (if available)
            stats.put("slowQueries", getSlowQueryCount());
            
            stats.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("Error getting performance stats", e);
            stats.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Test database connectivity
     */
    @GetMapping("/connectivity")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Test connectivity", description = "Test database connectivity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Connectivity test completed"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public ResponseEntity<Map<String, Object>> testConnectivity() {
        log.info("Testing database connectivity");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            
            try (Connection connection = dataSource.getConnection()) {
                boolean valid = connection.isValid(5);
                long responseTime = System.currentTimeMillis() - startTime;
                
                result.put("connected", true);
                result.put("valid", valid);
                result.put("responseTimeMs", responseTime);
                result.put("autoCommit", connection.getAutoCommit());
                result.put("transactionIsolation", connection.getTransactionIsolation());
                result.put("catalog", connection.getCatalog());
                
                // Test a simple query
                try (PreparedStatement stmt = connection.prepareStatement("SELECT 1 as test")) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            result.put("queryTest", "SUCCESS");
                            result.put("queryResult", rs.getInt("test"));
                        }
                    }
                }
                
            }
            
        } catch (Exception e) {
            log.error("Database connectivity test failed", e);
            result.put("connected", false);
            result.put("error", e.getMessage());
        }
        
        result.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(result);
    }

    /**
     * Spring Boot Actuator Health Indicator implementation
     */
    @Override
    public Health health() {
        try {
            boolean isHealthy = databaseHealthIndicator.isHealthy();
            
            if (isHealthy) {
                Map<String, Object> details = new HashMap<>();
                details.put("activeConnections", databaseHealthIndicator.getActiveConnections());
                details.put("idleConnections", databaseHealthIndicator.getIdleConnections());
                details.put("totalConnections", databaseHealthIndicator.getTotalConnections());
                
                return Health.up()
                    .withDetails(details)
                    .build();
            } else {
                return Health.down()
                    .withDetail("error", "Database connectivity check failed")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withException(e)
                .build();
        }
    }

    private Map<String, Object> getDatabaseStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // Get database size
            String sizeQuery = """
                SELECT 
                    table_schema as 'database_name',
                    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'size_mb',
                    COUNT(*) as 'table_count'
                FROM information_schema.tables 
                WHERE table_schema = DATABASE()
                GROUP BY table_schema
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(sizeQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("databaseName", rs.getString("database_name"));
                    stats.put("sizeInMB", rs.getDouble("size_mb"));
                    stats.put("tableCount", rs.getInt("table_count"));
                }
            }
            
            // Get top 5 largest tables
            String tablesQuery = """
                SELECT 
                    table_name,
                    table_rows,
                    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'size_mb'
                FROM information_schema.TABLES 
                WHERE table_schema = DATABASE()
                ORDER BY (data_length + index_length) DESC 
                LIMIT 5
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(tablesQuery);
                 ResultSet rs = stmt.executeQuery()) {
                
                Map<String, Map<String, Object>> tables = new HashMap<>();
                while (rs.next()) {
                    Map<String, Object> tableInfo = new HashMap<>();
                    tableInfo.put("rows", rs.getLong("table_rows"));
                    tableInfo.put("sizeMB", rs.getDouble("size_mb"));
                    tables.put(rs.getString("table_name"), tableInfo);
                }
                stats.put("largestTables", tables);
            }
            
        } catch (Exception e) {
            log.error("Error getting database statistics", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
    
    private int getActiveProcessCount() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SHOW PROCESSLIST");
             ResultSet rs = stmt.executeQuery()) {
            
            int count = 0;
            while (rs.next()) {
                count++;
            }
            return count;
            
        } catch (Exception e) {
            log.error("Error getting active process count", e);
            return -1;
        }
    }
    
    private int getSlowQueryCount() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                 "SHOW GLOBAL STATUS LIKE 'Slow_queries'");
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("Value");
            }
            return 0;
            
        } catch (Exception e) {
            log.error("Error getting slow query count", e);
            return -1;
        }
    }
}






