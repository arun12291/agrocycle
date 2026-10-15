package com.agrocycle.agrocycle.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications",
    indexes = {
        @Index(name = "idx_notif_recipient", columnList = "recipient_id"),
        @Index(name = "idx_notif_read", columnList = "is_read"),
        @Index(name = "idx_notif_created", columnList = "created_at")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private Long relatedOrderId;
    private Long relatedListingId;
    private Long relatedTicketId;

    @Column(name = "is_read")
    private boolean isRead = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    public Notification() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getRelatedOrderId() { return relatedOrderId; }
    public void setRelatedOrderId(Long relatedOrderId) { this.relatedOrderId = relatedOrderId; }
    public Long getRelatedListingId() { return relatedListingId; }
    public void setRelatedListingId(Long relatedListingId) { this.relatedListingId = relatedListingId; }
    public Long getRelatedTicketId() { return relatedTicketId; }
    public void setRelatedTicketId(Long relatedTicketId) { this.relatedTicketId = relatedTicketId; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    public enum NotificationType {
        NEW_ORDER_REQUEST, ORDER_ACCEPTED, ORDER_REJECTED, ORDER_CANCELLED,
        DELIVERY_OFFER, DELIVERY_ACCEPTED, DELIVERY_DECLINED,
        ORDER_PICKED_UP, ORDER_IN_TRANSIT, ORDER_DELIVERED, ORDER_COMPLETED,
        DELIVERY_FAILED, ACCOUNT_APPROVED, ACCOUNT_SUSPENDED,
        DELIVERY_PERSON_APPROVED, ADMIN_ANNOUNCEMENT, SUPPORT_TICKET_REPLY, SYSTEM_ALERT
    }
}
