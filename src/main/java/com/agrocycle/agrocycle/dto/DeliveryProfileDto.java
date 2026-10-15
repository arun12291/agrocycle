package com.agrocycle.agrocycle.dto;

import com.agrocycle.agrocycle.entity.DeliveryProfile;
import java.time.LocalDateTime;

public class DeliveryProfileDto {
    public Long id;
    public Long userId;
    public String fullName;
    public String email;
    public String mobileNumber;
    public String serviceArea;
    public String vehicleType;
    public Double carryingCapacityKg;
    public boolean available;
    public DeliveryProfile.ApprovalStatus approvalStatus;
    public Double averageRating;
    public Integer completedDeliveries;
    public LocalDateTime createdAt;

    public DeliveryProfileDto() {}

    public static DeliveryProfileDto from(DeliveryProfile dp) {
        DeliveryProfileDto dto = new DeliveryProfileDto();
        dto.id = dp.getId();
        dto.userId = dp.getUser().getId();
        dto.fullName = dp.getUser().getFullName();
        dto.email = dp.getUser().getEmail();
        dto.mobileNumber = dp.getUser().getMobileNumber();
        dto.serviceArea = dp.getServiceArea();
        dto.vehicleType = dp.getVehicleType();
        dto.carryingCapacityKg = dp.getCarryingCapacityKg();
        dto.available = dp.isAvailable();
        dto.approvalStatus = dp.getApprovalStatus();
        dto.averageRating = dp.getAverageRating();
        dto.completedDeliveries = dp.getCompletedDeliveries();
        dto.createdAt = dp.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getServiceArea() { return serviceArea; }
    public void setServiceArea(String serviceArea) { this.serviceArea = serviceArea; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public Double getCarryingCapacityKg() { return carryingCapacityKg; }
    public void setCarryingCapacityKg(Double carryingCapacityKg) { this.carryingCapacityKg = carryingCapacityKg; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public DeliveryProfile.ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(DeliveryProfile.ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public Integer getCompletedDeliveries() { return completedDeliveries; }
    public void setCompletedDeliveries(Integer completedDeliveries) { this.completedDeliveries = completedDeliveries; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
