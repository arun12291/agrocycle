package com.agrocycle.agrocycle.controller;

import com.agrocycle.agrocycle.dto.*;
import com.agrocycle.agrocycle.entity.*;
import com.agrocycle.agrocycle.exception.*;
import com.agrocycle.agrocycle.repository.*;
import com.agrocycle.agrocycle.service.AuthService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ListingRepository listingRepository;
    private final DeliveryProfileRepository deliveryProfileRepository;
    private final SupportTicketRepository ticketRepository;
    private final AuditLogRepository auditLogRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, OrderRepository orderRepository,
                            ListingRepository listingRepository, DeliveryProfileRepository deliveryProfileRepository,
                            SupportTicketRepository ticketRepository, AuditLogRepository auditLogRepository,
                            ReviewRepository reviewRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.listingRepository = listingRepository;
        this.deliveryProfileRepository = deliveryProfileRepository;
        this.ticketRepository = ticketRepository;
        this.auditLogRepository = auditLogRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---- Setup (unauthenticated, first-run only) ----
    @PostMapping("/setup")
    public ResponseEntity<?> setup(@RequestBody Map<String, String> body) {
        if (userRepository.existsByRole(User.Role.ADMIN))
            return ResponseEntity.status(409).body(Map.of("error", "Admin already exists"));
        User admin = new User();
        admin.setFullName(body.getOrDefault("fullName", "Administrator"));
        admin.setEmail(body.get("email").toLowerCase());
        admin.setMobileNumber(body.getOrDefault("mobileNumber", "9999999999"));
        admin.setPassword(passwordEncoder.encode(body.get("password")));
        admin.setRole(User.Role.ADMIN);
        admin.setStatus(User.AccountStatus.ACTIVE);
        admin.setVillage("Admin"); admin.setDistrict("Admin"); admin.setState("India");
        admin.setPreferredActivity(User.PreferredActivity.BOTH);
        return ResponseEntity.status(201).body(UserDto.from(userRepository.save(admin)));
    }

    // ---- Stats ----
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        long totalUsers = userRepository.count();
        long activeListings = listingRepository.countByStatus(Listing.ListingStatus.ACTIVE);
        long completedOrders = orderRepository.countByStatus(Order.OrderStatus.COMPLETED);
        BigDecimal totalMaterial = orderRepository.sumCompletedQuantity();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalUsers", totalUsers);
        m.put("activeListings", activeListings);
        m.put("completedTransactions", completedOrders);
        m.put("estimatedMaterialReusedKg", totalMaterial != null ? totalMaterial.multiply(BigDecimal.valueOf(1000)) : 0);
        return ResponseEntity.ok(m);
    }

    // ---- Users ----
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> listUsers(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userRepository.findAll(PageRequest.of(page, size))
            .stream().map(UserDto::from).collect(Collectors.toList()));
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUserStatus(@PathVariable Long id, @RequestParam String status,
            @AuthenticationPrincipal UserDetails adminDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(User.AccountStatus.valueOf(status));
        User saved = userRepository.save(user);
        logAudit(adminDetails.getUsername(), "USER_STATUS_CHANGE", id, null, null, "Status changed to " + status);
        return ResponseEntity.ok(UserDto.from(saved));
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable Long id, @RequestParam String role,
            @AuthenticationPrincipal UserDetails adminDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(User.Role.valueOf(role));
        User saved = userRepository.save(user);
        logAudit(adminDetails.getUsername(), "USER_ROLE_CHANGE", id, null, null, "Role changed to " + role);
        return ResponseEntity.ok(UserDto.from(saved));
    }

    // ---- Delivery Approvals ----
    @GetMapping("/delivery-applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeliveryProfileDto>> pendingDelivery() {
        return ResponseEntity.ok(deliveryProfileRepository.findByApprovalStatus(DeliveryProfile.ApprovalStatus.PENDING)
            .stream().map(DeliveryProfileDto::from).collect(Collectors.toList()));
    }

    @PostMapping("/delivery/{profileId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryProfileDto> approveDelivery(@PathVariable Long profileId,
            @AuthenticationPrincipal UserDetails adminDetails) {
        DeliveryProfile profile = deliveryProfileRepository.findById(profileId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        profile.setApprovalStatus(DeliveryProfile.ApprovalStatus.APPROVED);
        profile.setAvailable(true);
        profile.getUser().setStatus(User.AccountStatus.ACTIVE);
        userRepository.save(profile.getUser());
        DeliveryProfile saved = deliveryProfileRepository.save(profile);
        logAudit(adminDetails.getUsername(), "DELIVERY_PERSON_APPROVED", saved.getUser().getId(), null, null, null);
        return ResponseEntity.ok(DeliveryProfileDto.from(saved));
    }

    @PostMapping("/delivery/{profileId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> rejectDelivery(@PathVariable Long profileId,
            @RequestParam(required = false) String reason, @AuthenticationPrincipal UserDetails adminDetails) {
        DeliveryProfile profile = deliveryProfileRepository.findById(profileId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        profile.setApprovalStatus(DeliveryProfile.ApprovalStatus.REJECTED);
        profile.setAdminNotes(reason);
        deliveryProfileRepository.save(profile);
        logAudit(adminDetails.getUsername(), "DELIVERY_PERSON_REJECTED", profile.getUser().getId(), null, null, reason);
        return ResponseEntity.ok(Map.of("message", "Rejected"));
    }

    // ---- Support Tickets ----
    @GetMapping("/tickets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> allTickets() {
        return ResponseEntity.ok(ticketRepository.findAll(PageRequest.of(0, 50)).stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("subject", t.getSubject());
            m.put("userName", t.getUser().getFullName());
            m.put("category", t.getCategory());
            m.put("status", t.getStatus());
            m.put("createdAt", t.getCreatedAt());
            return m;
        }).collect(Collectors.toList()));
    }

    // ---- Audit Logs ----
    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> auditLogs(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(auditLogRepository.findAll(PageRequest.of(page, 30, org.springframework.data.domain.Sort.by("createdAt").descending())).stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("adminName", a.getAdmin().getFullName());
            m.put("action", a.getAction());
            m.put("targetUserId", a.getTargetUserId());
            m.put("details", a.getDetails());
            m.put("createdAt", a.getCreatedAt());
            return m;
        }).collect(Collectors.toList()));
    }

    private void logAudit(String adminEmail, String action, Long targetUser, Long targetOrder, Long targetListing, String details) {
        userRepository.findByEmail(adminEmail).ifPresent(admin -> {
            AuditLog log = new AuditLog();
            log.setAdmin(admin); log.setAction(action);
            log.setTargetUserId(targetUser); log.setTargetOrderId(targetOrder);
            log.setTargetListingId(targetListing); log.setDetails(details);
            auditLogRepository.save(log);
        });
    }
}
