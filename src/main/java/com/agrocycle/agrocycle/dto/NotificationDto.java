package com.agrocycle.agrocycle.dto;

import com.agrocycle.agrocycle.entity.Notification;
import java.time.LocalDateTime;

public class NotificationDto {
    public Long id;
    public Notification.NotificationType type;
    public String title;
    public String message;
    public Long relatedOrderId;
    public Long relatedListingId;
    public Long relatedTicketId;
    public boolean isRead;
    public LocalDateTime createdAt;

    public NotificationDto() {}

    public static NotificationDto from(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.id = n.getId();
        dto.type = n.getType();
        dto.title = n.getTitle();
        dto.message = n.getMessage();
        dto.relatedOrderId = n.getRelatedOrderId();
        dto.relatedListingId = n.getRelatedListingId();
        dto.relatedTicketId = n.getRelatedTicketId();
        dto.isRead = n.isRead();
        dto.createdAt = n.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Notification.NotificationType getType() { return type; }
    public void setType(Notification.NotificationType type) { this.type = type; }
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
}
