package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.FileMetadata;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.FileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    
    // Find by stored filename
    Optional<FileMetadata> findByStoredFilename(String storedFilename);
    
    // Find files by user
    List<FileMetadata> findByUploadedByOrderByCreatedAtDesc(User user);
    Page<FileMetadata> findByUploadedByOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find files by entity
    List<FileMetadata> findByEntityTypeAndEntityIdOrderByCreatedAtAsc(String entityType, Long entityId);
    
    // Find latest file by entity (for avatar, get the most recent one)
    Optional<FileMetadata> findFirstByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);
    
    // Find public files
    List<FileMetadata> findByIsPublicTrueOrderByCreatedAtDesc();
    Page<FileMetadata> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);
    
    // Find by file type
    List<FileMetadata> findByFileTypeOrderByCreatedAtDesc(FileType fileType);
    Page<FileMetadata> findByFileTypeOrderByCreatedAtDesc(FileType fileType, Pageable pageable);
    
    // Find by user and file type
    List<FileMetadata> findByUploadedByAndFileTypeOrderByCreatedAtDesc(User user, FileType fileType);
    
    // Search files by filename
    @Query("SELECT f FROM FileMetadata f WHERE " +
           "LOWER(f.originalFilename) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY f.createdAt DESC")
    List<FileMetadata> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT f FROM FileMetadata f WHERE " +
           "f.uploadedBy = :user AND (" +
           "LOWER(f.originalFilename) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY f.createdAt DESC")
    List<FileMetadata> searchByUserAndKeyword(@Param("user") User user, @Param("keyword") String keyword);
    
    // Statistics queries
    @Query("SELECT COUNT(f) FROM FileMetadata f WHERE f.uploadedBy = :user")
    Long countByUser(@Param("user") User user);
    
    @Query("SELECT SUM(f.fileSize) FROM FileMetadata f WHERE f.uploadedBy = :user")
    Long getTotalFileSizeByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(f) FROM FileMetadata f WHERE f.fileType = :fileType")
    Long countByFileType(@Param("fileType") FileType fileType);
    
    @Query("SELECT COUNT(f) FROM FileMetadata f WHERE f.createdAt >= :since")
    Long countFilesUploadedSince(@Param("since") LocalDateTime since);
    
    // Find orphaned files (files not associated with any entity)
    @Query("SELECT f FROM FileMetadata f WHERE f.entityType IS NULL OR f.entityId IS NULL")
    List<FileMetadata> findOrphanedFiles();
    
    // Find files larger than specified size
    @Query("SELECT f FROM FileMetadata f WHERE f.fileSize > :minSize ORDER BY f.fileSize DESC")
    List<FileMetadata> findLargeFiles(@Param("minSize") Long minSize);
    
    // Find files uploaded in date range
    @Query("SELECT f FROM FileMetadata f WHERE f.createdAt BETWEEN :startDate AND :endDate ORDER BY f.createdAt DESC")
    List<FileMetadata> findFilesUploadedBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    // Advanced search with multiple criteria
    @Query("SELECT f FROM FileMetadata f WHERE " +
           "(:user IS NULL OR f.uploadedBy = :user) AND " +
           "(:fileType IS NULL OR f.fileType = :fileType) AND " +
           "(:entityType IS NULL OR f.entityType = :entityType) AND " +
           "(:isPublic IS NULL OR f.isPublic = :isPublic) AND " +
           "(:keyword IS NULL OR LOWER(f.originalFilename) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY f.createdAt DESC")
    Page<FileMetadata> findWithCriteria(@Param("user") User user,
                                       @Param("fileType") FileType fileType,
                                       @Param("entityType") String entityType,
                                       @Param("isPublic") Boolean isPublic,
                                       @Param("keyword") String keyword,
                                       Pageable pageable);
}
