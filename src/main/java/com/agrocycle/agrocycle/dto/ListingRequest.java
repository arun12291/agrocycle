package com.agrocycle.agrocycle.dto;

import com.agrocycle.agrocycle.entity.Listing;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ListingRequest {
    @NotBlank @Size(max=200) public String productName;
    @NotNull public Listing.Category category;
    @Size(max=2000) public String description;
    @Size(max=100) public String condition;
    @NotNull @DecimalMin("0.01") public BigDecimal totalQuantity;
    @NotBlank @Size(max=20) public String unit;
    @NotNull @DecimalMin("0.00") public BigDecimal pricePerUnit;
    public boolean negotiable = false;
    @NotBlank public String village;
    @NotBlank public String district;
    @NotBlank public String state;
    @Size(max=10) public String postalCode;
    public Double latitude;
    public Double longitude;
    @NotNull public Listing.ListingType listingType;

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Listing.Category getCategory() { return category; }
    public void setCategory(Listing.Category category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public BigDecimal getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(BigDecimal totalQuantity) { this.totalQuantity = totalQuantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }
    public boolean isNegotiable() { return negotiable; }
    public void setNegotiable(boolean negotiable) { this.negotiable = negotiable; }
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
    public Listing.ListingType getListingType() { return listingType; }
    public void setListingType(Listing.ListingType listingType) { this.listingType = listingType; }
}
