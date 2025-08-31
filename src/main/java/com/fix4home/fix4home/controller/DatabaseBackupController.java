package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.service.DatabaseBackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * Database Backup Management Controller
 * Provides endpoints for database backup and restore operations
 */
@RestController
@RequestMapping("/api/v1/admin/database")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Database Backup", description = "Database backup and restore management")
@SecurityRequirement(name = "bearerAuth")
public class DatabaseBackupController {

    private final DatabaseBackupService databaseBackupService;

    /**
     * Get backup status and information
     */
    @GetMapping("/backup/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get backup status", description = "Get current backup status and information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Backup status retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public ResponseEntity<DatabaseBackupService.BackupStatus> getBackupStatus() {
        log.info("Getting backup status");
        DatabaseBackupService.BackupStatus status = databaseBackupService.getBackupStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * Trigger manual backup
     */
    @PostMapping("/backup/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Trigger manual backup", description = "Manually trigger a database backup")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Backup started successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Backup failed")
    })
    public ResponseEntity<BackupResponse> triggerBackup() {
        log.info("Manual backup triggered by admin");
        
        try {
            databaseBackupService.performBackupAsync();
            
            return ResponseEntity.accepted().body(new BackupResponse(
                true, 
                "Backup started successfully", 
                null,
                System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Failed to trigger backup", e);
            return ResponseEntity.internalServerError().body(new BackupResponse(
                false, 
                "Failed to start backup: " + e.getMessage(), 
                null,
                System.currentTimeMillis()
            ));
        }
    }

    /**
     * Restore database from backup
     */
    @PostMapping("/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restore database", description = "Restore database from a backup file")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Database restored successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid backup file path"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Restore failed")
    })
    public ResponseEntity<BackupResponse> restoreDatabase(
            @Parameter(description = "Path to backup file", required = true)
            @RequestParam String backupFilePath) {
        
        log.info("Database restore requested for file: {}", backupFilePath);
        
        try {
            databaseBackupService.restoreDatabase(backupFilePath);
            
            return ResponseEntity.ok(new BackupResponse(
                true, 
                "Database restored successfully from: " + backupFilePath, 
                backupFilePath,
                System.currentTimeMillis()
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid backup file path: {}", backupFilePath, e);
            return ResponseEntity.badRequest().body(new BackupResponse(
                false, 
                "Invalid backup file: " + e.getMessage(), 
                backupFilePath,
                System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Failed to restore database from: {}", backupFilePath, e);
            return ResponseEntity.internalServerError().body(new BackupResponse(
                false, 
                "Failed to restore database: " + e.getMessage(), 
                backupFilePath,
                System.currentTimeMillis()
            ));
        }
    }

    /**
     * Perform synchronous backup (for testing)
     */
    @PostMapping("/backup/sync")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Synchronous backup", description = "Perform synchronous backup (for testing purposes)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Backup completed successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Backup failed")
    })
    public ResponseEntity<BackupResponse> performSyncBackup() {
        log.info("Synchronous backup requested by admin");
        
        try {
            String backupPath = databaseBackupService.performBackup();
            
            return ResponseEntity.ok(new BackupResponse(
                true, 
                "Backup completed successfully", 
                backupPath,
                System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Failed to perform synchronous backup", e);
            return ResponseEntity.internalServerError().body(new BackupResponse(
                false, 
                "Failed to create backup: " + e.getMessage(), 
                null,
                System.currentTimeMillis()
            ));
        }
    }

    /**
     * Backup response DTO
     */
    public static class BackupResponse {
        private final boolean success;
        private final String message;
        private final String backupPath;
        private final long timestamp;

        public BackupResponse(boolean success, String message, String backupPath, long timestamp) {
            this.success = success;
            this.message = message;
            this.backupPath = backupPath;
            this.timestamp = timestamp;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getBackupPath() { return backupPath; }
        public long getTimestamp() { return timestamp; }
    }
}
