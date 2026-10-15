package com.agrocycle.agrocycle.dto;

import com.agrocycle.agrocycle.entity.Listing;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ListingDto {
    public Long id;
    public Long sellerId;
    public String sellerName;
    public String sellerMobile;
    public String productName;
    public Listing.Category category;
    public String description;
    public String condition;
    public BigDecimal totalQuantity;
    public BigDecimal availableQuantity;
    public String unit;
    public BigDecimal pricePerUnit;
    public boolean negotiable;
    public String village;
    public String district;
    public String state;
    public String postalCode;
    public Double latitude;
    public Double longitude;
    public Listing.ListingType listingType;
    public Listing.ListingStatus status;
    public List<String> imageUrls;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public Double averageRating;
    public Long ratingCount;

    public ListingDto() {}

    public static ListingDto from(Listing l) {
        ListingDto dto = new ListingDto();
        dto.id = l.getId();
        dto.sellerId = l.getSeller().getId();
        dto.sellerName = l.getSeller().getFullName();
        dto.productName = l.getProductName();
        dto.category = l.getCategory();
        dto.description = l.getDescription();
        dto.condition = l.getCondition();
        dto.totalQuantity = l.getTotalQuantity();
        dto.availableQuantity = l.getAvailableQuantity();
        dto.unit = l.getUnit();
        dto.pricePerUnit = l.getPricePerUnit();
        dto.negotiable = l.isNegotiable();
        dto.village = l.getVillage();
        dto.district = l.getDistrict();
        dto.state = l.getState();
        dto.postalCode = l.getPostalCode();
        dto.latitude = l.getLatitude();
        dto.longitude = l.getLongitude();
        dto.listingType = l.getListingType();
        dto.status = l.getStatus();
        dto.createdAt = l.getCreatedAt();
        dto.updatedAt = l.getUpdatedAt();
        if (l.getImages() != null) {
            dto.imageUrls = l.getImages().stream()
                .map(img -> "/uploads/" + img.getFileName())
                .collect(Collectors.toList());
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getSellerMobile() { return sellerMobile; }
    public void setSellerMobile(String sellerMobile) { this.sellerMobile = sellerMobile; }
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
    public BigDecimal getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(BigDecimal availableQuantity) { this.availableQuantity = availableQuantity; }
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
    public Listing.ListingStatus getStatus() { return status; }
    public void setStatus(Listing.ListingStatus status) { this.status = status; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public Long getRatingCount() { return ratingCount; }
    public void setRatingCount(Long ratingCount) { this.ratingCount = ratingCount; }
}
