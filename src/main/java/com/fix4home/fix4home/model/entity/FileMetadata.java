package com.fix4home.fix4home.model.entity;

import com.fix4home.fix4home.model.enums.FileType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata", indexes = {
    @Index(name = "idx_file_metadata_user_id", columnList = "user_id"),
    @Index(name = "idx_file_metadata_entity_type", columnList = "entity_type, entity_id"),
    @Index(name = "idx_file_metadata_file_type", columnList = "file_type"),
    @Index(name = "idx_file_metadata_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;
    
    @Column(name = "stored_filename", nullable = false, unique = true)
    private String storedFilename;
    
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    @Column(name = "file_url", nullable = false)
    private String fileUrl;
    
    @Column(name = "content_type", nullable = false)
    private String contentType;
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User uploadedBy;
    
    @Column(name = "entity_type")
    private String entityType; // 'SERVICE_REQUEST', 'SERVICE_POST', 'TECHNICIAN_PROFILE', etc.
    
    @Column(name = "entity_id")
    private Long entityId;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @Column(name = "width")
    private Integer width;
    
    @Column(name = "height")
    private Integer height;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Business Methods
    public boolean isImage() {
        return fileType == FileType.IMAGE;
    }
    
    public boolean isDocument() {
        return fileType == FileType.DOCUMENT;
    }
    
    public boolean isVideo() {
        return fileType == FileType.VIDEO;
    }
    
    public String getFileSizeFormatted() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
}
