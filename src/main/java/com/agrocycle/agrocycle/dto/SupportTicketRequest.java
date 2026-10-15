package com.agrocycle.agrocycle.dto;

import com.agrocycle.agrocycle.entity.SupportTicket;
import jakarta.validation.constraints.*;

public class SupportTicketRequest {
    @NotBlank @Size(max=200) public String subject;
    @NotNull public SupportTicket.TicketCategory category;
    @NotBlank @Size(max=2000) public String description;
    public Long relatedOrderId;

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public SupportTicket.TicketCategory getCategory() { return category; }
    public void setCategory(SupportTicket.TicketCategory category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getRelatedOrderId() { return relatedOrderId; }
    public void setRelatedOrderId(Long relatedOrderId) { this.relatedOrderId = relatedOrderId; }
}
