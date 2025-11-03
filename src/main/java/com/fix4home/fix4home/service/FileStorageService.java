package com.fix4home.fix4home.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {
    
    /**
     * Store a file and return the stored filename
     */
    String storeFile(MultipartFile file, String directory) throws IOException;
    
    /**
     * Load a file as Resource
     */
    Resource loadFileAsResource(String filename) throws IOException;
    
    /**
     * Get the file path
     */
    Path getFilePath(String filename);
    
    /**
     * Delete a file
     */
    boolean deleteFile(String filename);
    
    /**
     * Check if file exists
     */
    boolean fileExists(String filename);
    
    /**
     * Get file URL
     */
    String getFileUrl(String filename);
    
    /**
     * Initialize storage location
     */
    void init() throws IOException;
}
