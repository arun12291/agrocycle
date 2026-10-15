package com.agrocycle.agrocycle.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_admin", columnList = "admin_id"),
        @Index(name = "idx_audit_target", columnList = "target_user_id"),
        @Index(name = "idx_audit_created", columnList = "created_at")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Column(nullable = false)
    private String action;

    private Long targetUserId;
    private Long targetOrderId;
    private Long targetListingId;

    @Column(columnDefinition = "TEXT")
    private String details;

    private String ipAddress;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public AuditLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getAdmin() { return admin; }
    public void setAdmin(User admin) { this.admin = admin; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public Long getTargetOrderId() { return targetOrderId; }
    public void setTargetOrderId(Long targetOrderId) { this.targetOrderId = targetOrderId; }
    public Long getTargetListingId() { return targetListingId; }
    public void setTargetListingId(Long targetListingId) { this.targetListingId = targetListingId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
