package com.fix4home.fix4home.model.entity;

import com.fix4home.fix4home.model.enums.ConversationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Conversation entity represents a chat session between a customer and technician
 * It can be linked to ServiceRequest, ServicePost, or Consultation for context
 */
@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Business Context - one of these will be set to link conversation to business entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id")
    private ServiceRequest serviceRequest;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_post_id")
    private ServicePost servicePost;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id")
    private Consultation consultation;
    
    // Participants
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private User technician;
    
    // Conversation Management
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ConversationStatus status = ConversationStatus.ACTIVE;
    
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationship to messages
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages;
    
    // Helper methods
    public boolean isActive() {
        return this.status == ConversationStatus.ACTIVE;
    }
    
    public boolean canUserAccess(User user) {
        return this.customer.getId().equals(user.getId()) || 
               this.technician.getId().equals(user.getId());
    }
    
    public User getOtherParticipant(User currentUser) {
        if (this.customer.getId().equals(currentUser.getId())) {
            return this.technician;
        } else if (this.technician.getId().equals(currentUser.getId())) {
            return this.customer;
        }
        throw new IllegalArgumentException("User is not a participant in this conversation");
    }
} 