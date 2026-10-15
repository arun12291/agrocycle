package com.agrocycle.agrocycle.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class OrderRequest {
    @NotNull public Long listingId;
    @NotNull @DecimalMin("0.01") public BigDecimal requestedQuantity;
    @NotBlank public String deliveryAddress;
    @NotBlank public String deliveryVillage;
    @NotBlank public String deliveryDistrict;
    @NotBlank public String deliveryState;
    @Size(max=10) public String deliveryPostalCode;
    public Double deliveryLatitude;
    public Double deliveryLongitude;

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }
    public BigDecimal getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(BigDecimal requestedQuantity) { this.requestedQuantity = requestedQuantity; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getDeliveryVillage() { return deliveryVillage; }
    public void setDeliveryVillage(String deliveryVillage) { this.deliveryVillage = deliveryVillage; }
    public String getDeliveryDistrict() { return deliveryDistrict; }
    public void setDeliveryDistrict(String deliveryDistrict) { this.deliveryDistrict = deliveryDistrict; }
    public String getDeliveryState() { return deliveryState; }
    public void setDeliveryState(String deliveryState) { this.deliveryState = deliveryState; }
    public String getDeliveryPostalCode() { return deliveryPostalCode; }
    public void setDeliveryPostalCode(String deliveryPostalCode) { this.deliveryPostalCode = deliveryPostalCode; }
    public Double getDeliveryLatitude() { return deliveryLatitude; }
    public void setDeliveryLatitude(Double deliveryLatitude) { this.deliveryLatitude = deliveryLatitude; }
    public Double getDeliveryLongitude() { return deliveryLongitude; }
    public void setDeliveryLongitude(Double deliveryLongitude) { this.deliveryLongitude = deliveryLongitude; }
}
