package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.Conversation;
import com.fix4home.fix4home.model.entity.Message;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Message entity
 * Provides data access methods for chat messages
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Find messages by conversation with pagination (most recent first)
    Page<Message> findByConversationOrderBySentAtDesc(Conversation conversation, Pageable pageable);
    
    // Find messages by conversation with pagination (oldest first)
    Page<Message> findByConversationOrderBySentAtAsc(Conversation conversation, Pageable pageable);
    
    // Find latest message in conversation
    Optional<Message> findFirstByConversationOrderBySentAtDesc(Conversation conversation);
    
    // Count unread messages in conversation for specific user
    @Query("SELECT COUNT(m) FROM Message m WHERE " +
           "m.conversation = :conversation AND " +
           "m.isRead = false AND " +
           "m.sender != :user")
    long countUnreadByConversationAndUser(@Param("conversation") Conversation conversation, @Param("user") User user);
    
    // Find unread messages for user across all conversations
    @Query("SELECT m FROM Message m WHERE " +
           "m.isRead = false AND " +
           "m.sender != :user AND " +
           "(m.conversation.customer = :user OR m.conversation.technician = :user)")
    List<Message> findUnreadMessagesForUser(@Param("user") User user);
    
    // Mark messages as read
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE " +
           "m.conversation = :conversation AND " +
           "m.sender != :user AND " +
           "m.isRead = false")
    int markAllAsReadForUserInConversation(@Param("conversation") Conversation conversation, @Param("user") User user);
    
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.id IN :messageIds")
    int markMessagesAsRead(@Param("messageIds") List<Long> messageIds);
    
    // Find messages by type
    List<Message> findByConversationAndMessageType(Conversation conversation, MessageType messageType);
    
    // Find messages in date range
    @Query("SELECT m FROM Message m WHERE " +
           "m.conversation = :conversation AND " +
           "m.sentAt BETWEEN :startDate AND :endDate " +
           "ORDER BY m.sentAt ASC")
    List<Message> findByConversationAndDateRange(
            @Param("conversation") Conversation conversation,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Find messages before specific message (for pagination)
    @Query("SELECT m FROM Message m WHERE " +
           "m.conversation = :conversation AND " +
           "m.sentAt < :beforeDate " +
           "ORDER BY m.sentAt DESC")
    Page<Message> findByConversationBeforeDate(
            @Param("conversation") Conversation conversation,
            @Param("beforeDate") LocalDateTime beforeDate,
            Pageable pageable);
    
    // Find messages after specific message (for real-time updates)
    @Query("SELECT m FROM Message m WHERE " +
           "m.conversation = :conversation AND " +
           "m.sentAt > :afterDate " +
           "ORDER BY m.sentAt ASC")
    List<Message> findByConversationAfterDate(
            @Param("conversation") Conversation conversation,
            @Param("afterDate") LocalDateTime afterDate);
    
    // Count total messages in conversation
    long countByConversation(Conversation conversation);
    
    // Delete old messages (for cleanup)
    @Modifying
    @Query("DELETE FROM Message m WHERE m.sentAt < :beforeDate")
    int deleteMessagesBefore(@Param("beforeDate") LocalDateTime beforeDate);
    
    // Find system messages in conversation
    @Query("SELECT m FROM Message m WHERE " +
           "m.conversation = :conversation AND " +
           "m.messageType = 'SYSTEM' " +
           "ORDER BY m.sentAt DESC")
    List<Message> findSystemMessagesByConversation(@Param("conversation") Conversation conversation);
} 