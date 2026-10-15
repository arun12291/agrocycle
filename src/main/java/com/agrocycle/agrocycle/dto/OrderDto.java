package com.agrocycle.agrocycle.dto;

import com.agrocycle.agrocycle.entity.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDto {
    public Long id;
    public Long listingId;
    public String productName;
    public Long buyerId;
    public String buyerName;
    public String buyerMobile;
    public Long sellerId;
    public String sellerName;
    public String sellerMobile;
    public BigDecimal requestedQuantity;
    public String unit;
    public BigDecimal pricePerUnit;
    public BigDecimal totalAmount;
    public String deliveryAddress;
    public String deliveryDistrict;
    public String deliveryState;
    public Order.OrderStatus status;
    public String statusReason;
    public String deliveryPersonName;
    public String deliveryPersonMobile;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public LocalDateTime deliveredAt;
    public LocalDateTime completedAt;
    public BigDecimal deliveryFee;
    public String deliveryOtp;

    public OrderDto() {}

    public static OrderDto from(Order o, boolean includeSensitive, boolean includeOtp) {
        OrderDto dto = new OrderDto();
        dto.id = o.getId();
        dto.listingId = o.getListing().getId();
        dto.productName = o.getListing().getProductName();
        dto.buyerId = o.getBuyer().getId();
        dto.buyerName = o.getBuyer().getFullName();
        dto.sellerId = o.getSeller().getId();
        dto.sellerName = o.getSeller().getFullName();
        dto.requestedQuantity = o.getRequestedQuantity();
        dto.unit = o.getUnit();
        dto.pricePerUnit = o.getPricePerUnit();
        dto.totalAmount = o.getTotalAmount();
        dto.deliveryAddress = o.getDeliveryAddress();
        dto.deliveryDistrict = o.getDeliveryDistrict();
        dto.deliveryState = o.getDeliveryState();
        dto.status = o.getStatus();
        dto.statusReason = o.getStatusReason();
        dto.createdAt = o.getCreatedAt();
        dto.updatedAt = o.getUpdatedAt();
        dto.deliveredAt = o.getDeliveredAt();
        dto.completedAt = o.getCompletedAt();
        dto.deliveryFee = o.getDeliveryFee();
        if (includeSensitive) {
            dto.buyerMobile = o.getBuyer().getMobileNumber();
            dto.sellerMobile = o.getSeller().getMobileNumber();
        }
        if (includeOtp) {
            dto.deliveryOtp = o.getDeliveryOtp();
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getBuyerMobile() { return buyerMobile; }
    public void setBuyerMobile(String buyerMobile) { this.buyerMobile = buyerMobile; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getSellerMobile() { return sellerMobile; }
    public void setSellerMobile(String sellerMobile) { this.sellerMobile = sellerMobile; }
    public BigDecimal getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(BigDecimal requestedQuantity) { this.requestedQuantity = requestedQuantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getDeliveryDistrict() { return deliveryDistrict; }
    public void setDeliveryDistrict(String deliveryDistrict) { this.deliveryDistrict = deliveryDistrict; }
    public String getDeliveryState() { return deliveryState; }
    public void setDeliveryState(String deliveryState) { this.deliveryState = deliveryState; }
    public Order.OrderStatus getStatus() { return status; }
    public void setStatus(Order.OrderStatus status) { this.status = status; }
    public String getStatusReason() { return statusReason; }
    public void setStatusReason(String statusReason) { this.statusReason = statusReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
    public String getDeliveryOtp() { return deliveryOtp; }
    public void setDeliveryOtp(String deliveryOtp) { this.deliveryOtp = deliveryOtp; }
    public String getDeliveryPersonName() { return deliveryPersonName; }
    public void setDeliveryPersonName(String v) { this.deliveryPersonName = v; }
    public String getDeliveryPersonMobile() { return deliveryPersonMobile; }
    public void setDeliveryPersonMobile(String v) { this.deliveryPersonMobile = v; }
}
