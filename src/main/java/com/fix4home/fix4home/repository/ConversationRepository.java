package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.Conversation;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.ConversationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Conversation entity
 * Provides data access methods for chat conversations
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    // Find conversations by participant
    @Query("SELECT c FROM Conversation c WHERE c.customer = :user OR c.technician = :user")
    List<Conversation> findByParticipant(@Param("user") User user);
    
    @Query("SELECT c FROM Conversation c WHERE (c.customer = :user OR c.technician = :user) AND c.status = :status")
    List<Conversation> findByParticipantAndStatus(@Param("user") User user, @Param("status") ConversationStatus status);
    
    @Query("SELECT c FROM Conversation c WHERE (c.customer = :user OR c.technician = :user) ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC")
    Page<Conversation> findByParticipantOrderByLastMessage(@Param("user") User user, Pageable pageable);
    
    // Find conversation by business context
    Optional<Conversation> findByServiceRequestId(Long serviceRequestId);
    Optional<Conversation> findByServicePostId(Long servicePostId);
    Optional<Conversation> findByConsultationId(Long consultationId);
    
    // Find existing conversation between two users
    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.customer = :user1 AND c.technician = :user2) OR " +
           "(c.customer = :user2 AND c.technician = :user1)")
    Optional<Conversation> findByParticipants(@Param("user1") User user1, @Param("user2") User user2);
    
    // Find conversation between two users for specific business context
    @Query("SELECT c FROM Conversation c WHERE " +
           "((c.customer = :customer AND c.technician = :technician) OR " +
           " (c.customer = :technician AND c.technician = :customer)) AND " +
           "c.serviceRequestId = :serviceRequestId")
    Optional<Conversation> findByParticipantsAndServiceRequest(
            @Param("customer") User customer, 
            @Param("technician") User technician, 
            @Param("serviceRequestId") Long serviceRequestId);
    
    // Count conversations by status
    long countByStatus(ConversationStatus status);
    
    // Find active conversations for user
    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.customer = :user OR c.technician = :user) AND " +
           "c.status = 'ACTIVE' " +
           "ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC")
    List<Conversation> findActiveConversationsByUser(@Param("user") User user);
    
    // Check if conversation exists between participants with business context
    @Query("SELECT COUNT(c) > 0 FROM Conversation c WHERE " +
           "c.customer = :customer AND c.technician = :technician AND " +
           "(c.serviceRequestId = :serviceRequestId OR " +
           " c.servicePostId = :servicePostId OR " +
           " c.consultationId = :consultationId)")
    boolean existsByParticipantsAndBusinessContext(
            @Param("customer") User customer,
            @Param("technician") User technician,
            @Param("serviceRequestId") Long serviceRequestId,
            @Param("servicePostId") Long servicePostId,
            @Param("consultationId") Long consultationId);
} 