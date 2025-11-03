package com.fix4home.fix4home.service;

import com.fix4home.fix4home.config.FileStorageConfig;
import com.fix4home.fix4home.exception.FileProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageProcessingService {
    
    private final FileStorageConfig fileStorageConfig;
    
    /**
     * Create thumbnail for an image
     */
    public String createThumbnail(Path originalImagePath, String originalFileName) {
        try {
            BufferedImage originalImage = ImageIO.read(originalImagePath.toFile());
            if (originalImage == null) {
                throw new FileProcessingException("Could not read image file: " + originalFileName);
            }
            
            // Create thumbnail
            BufferedImage thumbnail = Scalr.resize(
                originalImage,
                Scalr.Method.QUALITY,
                Scalr.Mode.FIT_TO_WIDTH,
                fileStorageConfig.getImage().getThumbnail().getWidth(),
                fileStorageConfig.getImage().getThumbnail().getHeight()
            );
            
            // Generate thumbnail filename
            String extension = getFileExtension(originalFileName);
            String thumbnailFileName = "thumb_" + originalFileName;
            
            // Save thumbnail
            Path thumbnailPath = originalImagePath.getParent().resolve(thumbnailFileName);
            ImageIO.write(thumbnail, extension, thumbnailPath.toFile());
            
            log.info("Thumbnail created: {}", thumbnailFileName);
            return thumbnailPath.toString();
            
        } catch (IOException ex) {
            log.error("Error creating thumbnail for: {}", originalFileName, ex);
            throw new FileProcessingException("Could not create thumbnail for image: " + originalFileName, ex);
        }
    }
    
    /**
     * Resize image if it exceeds maximum dimensions
     */
    public boolean resizeImageIfNeeded(Path imagePath, String fileName) {
        try {
            BufferedImage originalImage = ImageIO.read(imagePath.toFile());
            if (originalImage == null) {
                return false;
            }
            
            int maxWidth = fileStorageConfig.getImage().getMaxWidth();
            int maxHeight = fileStorageConfig.getImage().getMaxHeight();
            
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            // Check if resizing is needed
            if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
                return false; // No resizing needed
            }
            
            // Calculate new dimensions while maintaining aspect ratio
            double aspectRatio = (double) originalWidth / originalHeight;
            int newWidth = maxWidth;
            int newHeight = (int) (maxWidth / aspectRatio);
            
            if (newHeight > maxHeight) {
                newHeight = maxHeight;
                newWidth = (int) (maxHeight * aspectRatio);
            }
            
            // Resize the image
            BufferedImage resizedImage = Scalr.resize(
                originalImage,
                Scalr.Method.QUALITY,
                Scalr.Mode.FIT_EXACT,
                newWidth,
                newHeight
            );
            
            // Save resized image
            String extension = getFileExtension(fileName);
            ImageIO.write(resizedImage, extension, imagePath.toFile());
            
            log.info("Image resized: {} from {}x{} to {}x{}", fileName, originalWidth, originalHeight, newWidth, newHeight);
            return true;
            
        } catch (IOException ex) {
            log.error("Error resizing image: {}", fileName, ex);
            throw new FileProcessingException("Could not resize image: " + fileName, ex);
        }
    }
    
    /**
     * Get image dimensions
     */
    public ImageDimensions getImageDimensions(Path imagePath) {
        try {
            BufferedImage image = ImageIO.read(imagePath.toFile());
            if (image == null) {
                return null;
            }
            
            return new ImageDimensions(image.getWidth(), image.getHeight());
            
        } catch (IOException ex) {
            log.error("Error reading image dimensions: {}", imagePath, ex);
            return null;
        }
    }
    
    /**
     * Validate if file is a valid image
     */
    public boolean isValidImage(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            return image != null;
        } catch (IOException ex) {
            return false;
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "jpg";
        }
        
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "jpg";
        }
        
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    public static class ImageDimensions {
        private final int width;
        private final int height;
        
        public ImageDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
    }
}
