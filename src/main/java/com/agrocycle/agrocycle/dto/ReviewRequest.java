package com.agrocycle.agrocycle.dto;

import jakarta.validation.constraints.*;

public class ReviewRequest {
    @NotNull public Long orderId;
    @NotNull @Min(1) @Max(5) public Integer rating;
    @Size(max=1000) public String comment;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
