package com.agrocycle.agrocycle.dto;

import jakarta.validation.constraints.*;

public class DeliveryProfileRequest {
    @NotBlank public String fullName;
    @NotBlank @Email public String email;
    @NotBlank @Pattern(regexp="^[6-9]\\d{9}$", message="Enter valid 10-digit mobile") public String mobileNumber;
    @NotBlank @Size(min=8) public String password;
    @NotBlank public String village;
    @NotBlank public String district;
    @NotBlank public String state;
    @Size(max=10) public String postalCode;
    public Double latitude;
    public Double longitude;
    @NotBlank public String serviceArea;
    public String vehicleType;
    public Double carryingCapacityKg;
    public String documentType;
    public String documentNumber;

    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String v) { this.mobileNumber = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v; }
    public String getVillage() { return village; }
    public void setVillage(String v) { this.village = v; }
    public String getDistrict() { return district; }
    public void setDistrict(String v) { this.district = v; }
    public String getState() { return state; }
    public void setState(String v) { this.state = v; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String v) { this.postalCode = v; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double v) { this.latitude = v; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double v) { this.longitude = v; }
    public String getServiceArea() { return serviceArea; }
    public void setServiceArea(String v) { this.serviceArea = v; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String v) { this.vehicleType = v; }
    public Double getCarryingCapacityKg() { return carryingCapacityKg; }
    public void setCarryingCapacityKg(Double v) { this.carryingCapacityKg = v; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String v) { this.documentType = v; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String v) { this.documentNumber = v; }
}
