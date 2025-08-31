package com.fix4home.fix4home.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPOutputStream;
import java.io.FileOutputStream;
import java.nio.file.StandardCopyOption;

/**
 * Database Backup Service
 * Handles automated database backups with scheduling and retention management
 */
@Slf4j
@Service
public class DatabaseBackupService {

    @Value("${database.backup.enabled:true}")
    private boolean backupEnabled;

    @Value("${database.backup.location:backup/database/}")
    private String backupLocation;

    @Value("${database.backup.retention.days:30}")
    private int retentionDays;

    @Value("${database.backup.compress:true}")
    private boolean compressBackups;

    @Value("${database.backup.mysql.bin.path:/usr/bin/}")
    private String mysqlBinPath;

    @Value("${database.backup.mysql.host:localhost}")
    private String dbHost;

    @Value("${database.backup.mysql.port:3307}")
    private String dbPort;

    @Value("${database.backup.mysql.database:fix4home_db}")
    private String dbName;

    @Value("${database.backup.mysql.username:root}")
    private String dbUsername;

    @Value("${database.backup.mysql.password:pass123}")
    private String dbPassword;

    @Value("${database.backup.notification.enabled:true}")
    private boolean notificationEnabled;

    private static final DateTimeFormatter BACKUP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Scheduled backup job - runs daily at 2 AM
     */
    @Scheduled(cron = "${database.backup.schedule.cron:0 0 2 * * ?}")
    public void scheduledBackup() {
        if (!backupEnabled) {
            log.info("Database backup is disabled, skipping scheduled backup");
            return;
        }

        log.info("Starting scheduled database backup");
        performBackupAsync();
    }

    /**
     * Perform backup asynchronously
     */
    @Async
    public CompletableFuture<String> performBackupAsync() {
        try {
            String backupPath = performBackup();
            log.info("Database backup completed successfully: {}", backupPath);
            return CompletableFuture.completedFuture(backupPath);
        } catch (Exception e) {
            log.error("Database backup failed", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Perform manual backup
     */
    public String performBackup() throws IOException, InterruptedException {
        validateConfiguration();
        
        // Create backup directory if it doesn't exist
        Path backupDir = Paths.get(backupLocation);
        Files.createDirectories(backupDir);

        // Generate backup filename with timestamp
        String timestamp = LocalDateTime.now().format(BACKUP_DATE_FORMAT);
        String backupFileName = String.format("%s_backup_%s.sql", dbName, timestamp);
        Path backupFilePath = backupDir.resolve(backupFileName);

        // Perform mysqldump
        performMysqlDump(backupFilePath);

        // Compress if enabled
        if (compressBackups) {
            backupFilePath = compressBackup(backupFilePath);
        }

        // Clean up old backups
        cleanupOldBackups();

        // Send notification if enabled
        if (notificationEnabled) {
            sendBackupNotification(backupFilePath.toString(), true, null);
        }

        return backupFilePath.toString();
    }

    /**
     * Restore database from backup file
     */
    public void restoreDatabase(String backupFilePath) throws IOException, InterruptedException {
        validateConfiguration();

        Path backupPath = Paths.get(backupFilePath);
        if (!Files.exists(backupPath)) {
            throw new IllegalArgumentException("Backup file does not exist: " + backupFilePath);
        }

        log.info("Starting database restore from: {}", backupFilePath);

        // Decompress if it's a compressed file
        Path sqlFilePath = backupPath;
        if (backupFilePath.endsWith(".gz")) {
            sqlFilePath = decompressBackup(backupPath);
        }

        // Perform mysql restore
        performMysqlRestore(sqlFilePath);

        log.info("Database restore completed successfully");

        // Clean up temporary decompressed file if created
        if (!sqlFilePath.equals(backupPath)) {
            Files.deleteIfExists(sqlFilePath);
        }
    }

    private void validateConfiguration() {
        if (!backupEnabled) {
            throw new IllegalStateException("Database backup is disabled");
        }

        if (dbHost == null || dbPort == null || dbName == null || dbUsername == null) {
            throw new IllegalStateException("Database connection parameters are not properly configured");
        }
    }

    private void performMysqlDump(Path backupFilePath) throws IOException, InterruptedException {
        String mysqldumpPath = Paths.get(mysqlBinPath, "mysqldump").toString();
        
        ProcessBuilder processBuilder = new ProcessBuilder(
            mysqldumpPath,
            "--host=" + dbHost,
            "--port=" + dbPort,
            "--user=" + dbUsername,
            "--password=" + dbPassword,
            "--single-transaction",
            "--routines",
            "--triggers",
            "--quick",
            "--lock-tables=false",
            "--set-gtid-purged=OFF",
            dbName
        );

        processBuilder.redirectOutput(backupFilePath.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("mysqldump failed with exit code: " + exitCode);
        }

        log.info("MySQL dump completed: {}", backupFilePath);
    }

    private void performMysqlRestore(Path sqlFilePath) throws IOException, InterruptedException {
        String mysqlPath = Paths.get(mysqlBinPath, "mysql").toString();

        ProcessBuilder processBuilder = new ProcessBuilder(
            mysqlPath,
            "--host=" + dbHost,
            "--port=" + dbPort,
            "--user=" + dbUsername,
            "--password=" + dbPassword,
            dbName
        );

        processBuilder.redirectInput(sqlFilePath.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("mysql restore failed with exit code: " + exitCode);
        }

        log.info("MySQL restore completed from: {}", sqlFilePath);
    }

    private Path compressBackup(Path backupFilePath) throws IOException {
        Path compressedPath = Paths.get(backupFilePath.toString() + ".gz");

        try (FileOutputStream fos = new FileOutputStream(compressedPath.toFile());
             GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {
            
            Files.copy(backupFilePath, gzipOS);
        }

        // Delete original uncompressed file
        Files.deleteIfExists(backupFilePath);

        log.info("Backup compressed: {}", compressedPath);
        return compressedPath;
    }

    private Path decompressBackup(Path compressedPath) throws IOException {
        String originalFileName = compressedPath.getFileName().toString();
        if (originalFileName.endsWith(".gz")) {
            originalFileName = originalFileName.substring(0, originalFileName.length() - 3);
        }

        Path decompressedPath = compressedPath.getParent().resolve("temp_" + originalFileName);

        try (java.util.zip.GZIPInputStream gzipIS = new java.util.zip.GZIPInputStream(
                Files.newInputStream(compressedPath))) {
            Files.copy(gzipIS, decompressedPath, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("Backup decompressed: {}", decompressedPath);
        return decompressedPath;
    }

    private void cleanupOldBackups() {
        try {
            Path backupDir = Paths.get(backupLocation);
            if (!Files.exists(backupDir)) {
                return;
            }

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

            Files.list(backupDir)
                .filter(path -> path.getFileName().toString().contains("_backup_"))
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toInstant()
                            .isBefore(cutoffDate.atZone(java.time.ZoneId.systemDefault()).toInstant());
                    } catch (IOException e) {
                        log.warn("Error checking file modification time: {}", path, e);
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                        log.info("Deleted old backup: {}", path);
                    } catch (IOException e) {
                        log.warn("Failed to delete old backup: {}", path, e);
                    }
                });

        } catch (IOException e) {
            log.error("Error during backup cleanup", e);
        }
    }

    private void sendBackupNotification(String backupPath, boolean success, String errorMessage) {
        // This would integrate with your notification service
        // For now, just log the notification
        if (success) {
            log.info("BACKUP NOTIFICATION: Database backup completed successfully at {}", backupPath);
        } else {
            log.error("BACKUP NOTIFICATION: Database backup failed - {}", errorMessage);
        }
    }

    /**
     * Get backup status and information
     */
    public BackupStatus getBackupStatus() {
        try {
            Path backupDir = Paths.get(backupLocation);
            if (!Files.exists(backupDir)) {
                return new BackupStatus(false, 0, null, "Backup directory does not exist");
            }

            long backupCount = Files.list(backupDir)
                .filter(path -> path.getFileName().toString().contains("_backup_"))
                .count();

            Path latestBackup = Files.list(backupDir)
                .filter(path -> path.getFileName().toString().contains("_backup_"))
                .max((p1, p2) -> {
                    try {
                        return Files.getLastModifiedTime(p1).compareTo(Files.getLastModifiedTime(p2));
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .orElse(null);

            return new BackupStatus(backupEnabled, backupCount, 
                latestBackup != null ? latestBackup.toString() : null, null);

        } catch (IOException e) {
            return new BackupStatus(false, 0, null, "Error reading backup directory: " + e.getMessage());
        }
    }

    /**
     * Backup status information
     */
    public static class BackupStatus {
        private final boolean enabled;
        private final long backupCount;
        private final String latestBackup;
        private final String errorMessage;

        public BackupStatus(boolean enabled, long backupCount, String latestBackup, String errorMessage) {
            this.enabled = enabled;
            this.backupCount = backupCount;
            this.latestBackup = latestBackup;
            this.errorMessage = errorMessage;
        }

        public boolean isEnabled() { return enabled; }
        public long getBackupCount() { return backupCount; }
        public String getLatestBackup() { return latestBackup; }
        public String getErrorMessage() { return errorMessage; }
    }
}
