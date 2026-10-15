package com.agrocycle.agrocycle.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_offers",
    indexes = {
        @Index(name = "idx_doffer_order", columnList = "order_id"),
        @Index(name = "idx_doffer_delivery", columnList = "delivery_profile_id"),
        @Index(name = "idx_doffer_status", columnList = "status")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class DeliveryOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_profile_id", nullable = false)
    private DeliveryProfile deliveryProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status = OfferStatus.PENDING;

    private Double distanceKm;
    private Double score;
    private BigDecimal offerFee = BigDecimal.ZERO;
    private LocalDateTime offerExpiresAt;
    private LocalDateTime respondedAt;
    private String declineReason;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public DeliveryOffer() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public DeliveryProfile getDeliveryProfile() { return deliveryProfile; }
    public void setDeliveryProfile(DeliveryProfile deliveryProfile) { this.deliveryProfile = deliveryProfile; }
    public OfferStatus getStatus() { return status; }
    public void setStatus(OfferStatus status) { this.status = status; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public BigDecimal getOfferFee() { return offerFee; }
    public void setOfferFee(BigDecimal offerFee) { this.offerFee = offerFee; }
    public LocalDateTime getOfferExpiresAt() { return offerExpiresAt; }
    public void setOfferExpiresAt(LocalDateTime offerExpiresAt) { this.offerExpiresAt = offerExpiresAt; }
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
    public String getDeclineReason() { return declineReason; }
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum OfferStatus { PENDING, ACCEPTED, DECLINED, EXPIRED, CANCELLED }
}
