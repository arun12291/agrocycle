package com.agrocycle.agrocycle.dto;

import com.agrocycle.agrocycle.entity.User;
import java.time.LocalDateTime;

public class UserDto {
    public Long id;
    public String fullName;
    public String email;
    public String mobileNumber;
    public User.Role role;
    public User.AccountStatus status;
    public String village;
    public String district;
    public String state;
    public String postalCode;
    public Double latitude;
    public Double longitude;
    public User.PreferredActivity preferredActivity;
    public LocalDateTime createdAt;
    public LocalDateTime lastLoginAt;

    public UserDto() {}

    public static UserDto from(User u) {
        UserDto dto = new UserDto();
        dto.id = u.getId();
        dto.fullName = u.getFullName();
        dto.email = u.getEmail();
        dto.mobileNumber = u.getMobileNumber();
        dto.role = u.getRole();
        dto.status = u.getStatus();
        dto.village = u.getVillage();
        dto.district = u.getDistrict();
        dto.state = u.getState();
        dto.postalCode = u.getPostalCode();
        dto.latitude = u.getLatitude();
        dto.longitude = u.getLongitude();
        dto.preferredActivity = u.getPreferredActivity();
        dto.createdAt = u.getCreatedAt();
        dto.lastLoginAt = u.getLastLoginAt();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public User.Role getRole() { return role; }
    public void setRole(User.Role role) { this.role = role; }
    public User.AccountStatus getStatus() { return status; }
    public void setStatus(User.AccountStatus status) { this.status = status; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public User.PreferredActivity getPreferredActivity() { return preferredActivity; }
    public void setPreferredActivity(User.PreferredActivity preferredActivity) { this.preferredActivity = preferredActivity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
