package com.agrocycle.agrocycle.controller;

import com.agrocycle.agrocycle.dto.*;
import com.agrocycle.agrocycle.entity.*;
import com.agrocycle.agrocycle.exception.*;
import com.agrocycle.agrocycle.notification.NotificationService;
import com.agrocycle.agrocycle.repository.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    private final SupportTicketRepository ticketRepository;
    private final TicketReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public SupportController(SupportTicketRepository ticketRepository, TicketReplyRepository replyRepository,
                              UserRepository userRepository, NotificationService notificationService) {
        this.ticketRepository = ticketRepository;
        this.replyRepository = replyRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @PostMapping("/tickets")
    public ResponseEntity<Map<String, Object>> createTicket(@Valid @RequestBody SupportTicketRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        SupportTicket ticket = new SupportTicket();
        ticket.setUser(user);
        ticket.setSubject(req.getSubject());
        ticket.setCategory(req.getCategory());
        ticket.setDescription(req.getDescription());
        ticket.setRelatedOrderId(req.getRelatedOrderId());
        ticket.setStatus(SupportTicket.TicketStatus.OPEN);
        SupportTicket saved = ticketRepository.save(ticket);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", saved.getId());
        result.put("message", "Ticket created successfully");
        return ResponseEntity.status(201).body(result);
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<Map<String, Object>>> myTickets(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        return ResponseEntity.ok(ticketRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
            .stream().map(this::toMap).collect(Collectors.toList()));
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<Map<String, Object>> getTicket(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        SupportTicket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        if (!ticket.getUser().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN)
            throw new ApiException("Not authorized", 403);
        Map<String, Object> result = toMap(ticket);
        result.put("replies", replyRepository.findByTicketIdOrderByCreatedAtAsc(id)
            .stream().map(r -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", r.getId());
                m.put("message", r.getMessage());
                m.put("authorName", r.getAuthor().getFullName());
                m.put("isAdminReply", r.isAdminReply());
                m.put("createdAt", r.getCreatedAt());
                return m;
            }).collect(Collectors.toList()));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/tickets/{id}/reply")
    public ResponseEntity<Map<String, String>> reply(@PathVariable Long id,
            @RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        SupportTicket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        if (!ticket.getUser().getId().equals(user.getId()) && !isAdmin) throw new ApiException("Not authorized", 403);
        TicketReply reply = new TicketReply();
        reply.setTicket(ticket);
        reply.setAuthor(user);
        reply.setMessage(body.getOrDefault("message", "").trim());
        reply.setAdminReply(isAdmin);
        replyRepository.save(reply);
        if (isAdmin) {
            ticket.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
            ticketRepository.save(ticket);
            notificationService.sendTicketNotification(ticket.getUser(), Notification.NotificationType.SUPPORT_TICKET_REPLY,
                "Support Reply: " + ticket.getSubject(), "Support team replied: " + reply.getMessage(), ticket.getId());
        }
        return ResponseEntity.ok(Map.of("message", "Reply added"));
    }

    private Map<String, Object> toMap(SupportTicket t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("subject", t.getSubject());
        m.put("category", t.getCategory());
        m.put("status", t.getStatus());
        m.put("createdAt", t.getCreatedAt());
        m.put("updatedAt", t.getUpdatedAt());
        return m;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
