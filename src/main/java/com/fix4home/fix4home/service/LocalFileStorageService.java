package com.fix4home.fix4home.service;

import com.fix4home.fix4home.config.FileStorageConfig;
import com.fix4home.fix4home.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalFileStorageService implements FileStorageService {
    
    private final FileStorageConfig fileStorageConfig;
    private Path fileStorageLocation;
    
    @Override
    public void init() throws IOException {
        this.fileStorageLocation = Paths.get(fileStorageConfig.getLocal().getUploadDir())
                .toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage directory initialized: {}", this.fileStorageLocation);
            log.info("File storage directory exists: {}", Files.exists(this.fileStorageLocation));
        } catch (Exception ex) {
            log.error("Failed to create file storage directory: {}", this.fileStorageLocation, ex);
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }
    
    @Override
    public String storeFile(MultipartFile file, String directory) throws IOException {
        // Normalize file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            // Check if the file's name contains invalid characters
            if (originalFileName.contains("..")) {
                throw new FileStorageException("Filename contains invalid path sequence " + originalFileName);
            }
            
            // Generate unique filename
            String fileExtension = getFileExtension(originalFileName);
            String storedFileName = UUID.randomUUID().toString() + "." + fileExtension;
            
            // Create directory if not exists
            Path targetDirectory = this.fileStorageLocation;
            if (directory != null && !directory.isEmpty()) {
                targetDirectory = this.fileStorageLocation.resolve(directory);
                Files.createDirectories(targetDirectory);
            }
            
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = targetDirectory.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("File stored successfully: {} -> {}", originalFileName, storedFileName);
            
            // Return relative path from upload directory
            String relativePath = this.fileStorageLocation.relativize(targetLocation).toString();
            return relativePath.replace("\\", "/"); // Normalize path separators
            
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }
    
    @Override
    public Resource loadFileAsResource(String filename) throws IOException {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            log.debug("Loading file - filename: {}, storage location: {}, resolved path: {}", 
                    filename, this.fileStorageLocation, filePath);
            
            // Security check: ensure the resolved path is within the storage location
            if (!filePath.startsWith(this.fileStorageLocation)) {
                log.error("Security violation: Attempted to access file outside storage directory: {}", filePath);
                throw new FileStorageException("Access denied: Invalid file path");
            }
            
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                log.debug("File found: {}", filePath);
                return resource;
            } else {
                log.error("File not found - filename: {}, resolved path: {}, exists: {}", 
                        filename, filePath, Files.exists(filePath));
                throw new FileStorageException("File not found: " + filename + " (resolved to: " + filePath + ")");
            }
        } catch (MalformedURLException ex) {
            log.error("Malformed URL for file: {}", filename, ex);
            throw new FileStorageException("File not found " + filename, ex);
        }
    }
    
    @Override
    public Path getFilePath(String filename) {
        return this.fileStorageLocation.resolve(filename).normalize();
    }
    
    @Override
    public boolean deleteFile(String filename) {
        try {
            Path filePath = getFilePath(filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.error("Error deleting file: {}", filename, ex);
            return false;
        }
    }
    
    @Override
    public boolean fileExists(String filename) {
        Path filePath = getFilePath(filename);
        return Files.exists(filePath);
    }
    
    @Override
    public String getFileUrl(String filename) {
        return fileStorageConfig.getLocal().getBaseUrl() + "download/" + filename;
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}
