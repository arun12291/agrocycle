package com.agrocycle.agrocycle.dto;

import com.agrocycle.agrocycle.entity.User;
import jakarta.validation.constraints.*;

public class RegisterRequest {
    @NotBlank @Size(min=2, max=100) public String fullName;
    @NotBlank @Email public String email;
    @NotBlank @Pattern(regexp="^[6-9]\\d{9}$", message="Enter valid 10-digit mobile") public String mobileNumber;
    @NotBlank @Size(min=8, message="Password must be at least 8 characters") public String password;
    @NotBlank public String confirmPassword;
    @NotBlank public String village;
    @NotBlank public String district;
    @NotBlank public String state;
    @Size(max=10) public String postalCode;
    public Double latitude;
    public Double longitude;
    public User.PreferredActivity preferredActivity = User.PreferredActivity.BOTH;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
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
}
