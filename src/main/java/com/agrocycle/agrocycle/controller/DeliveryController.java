package com.agrocycle.agrocycle.controller;

import com.agrocycle.agrocycle.dto.*;
import com.agrocycle.agrocycle.entity.*;
import com.agrocycle.agrocycle.exception.*;
import com.agrocycle.agrocycle.repository.*;
import com.agrocycle.agrocycle.service.DeliveryMatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryProfileRepository deliveryProfileRepository;
    private final DeliveryOfferRepository deliveryOfferRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DeliveryMatchingService deliveryMatchingService;

    public DeliveryController(DeliveryProfileRepository deliveryProfileRepository,
                               DeliveryOfferRepository deliveryOfferRepository,
                               OrderRepository orderRepository, UserRepository userRepository,
                               DeliveryMatchingService deliveryMatchingService) {
        this.deliveryProfileRepository = deliveryProfileRepository;
        this.deliveryOfferRepository = deliveryOfferRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.deliveryMatchingService = deliveryMatchingService;
    }

    @GetMapping("/my-profile")
    public ResponseEntity<DeliveryProfileDto> myProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        DeliveryProfile profile = deliveryProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Delivery profile not found"));
        return ResponseEntity.ok(DeliveryProfileDto.from(profile));
    }

    @PostMapping("/toggle-availability")
    public ResponseEntity<Map<String, Object>> toggleAvailability(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        DeliveryProfile profile = deliveryProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Delivery profile not found"));
        if (profile.getApprovalStatus() != DeliveryProfile.ApprovalStatus.APPROVED)
            throw new ApiException("Profile not approved", 403);
        profile.setAvailable(!profile.isAvailable());
        deliveryProfileRepository.save(profile);
        return ResponseEntity.ok(Map.of("available", profile.isAvailable()));
    }

    @GetMapping("/my-offers")
    public ResponseEntity<List<Map<String, Object>>> myOffers(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        DeliveryProfile profile = deliveryProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Delivery profile not found"));
        return ResponseEntity.ok(
            deliveryOfferRepository.findByDeliveryProfileIdAndStatus(profile.getId(), DeliveryOffer.OfferStatus.PENDING)
                .stream().map(o -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("offerId", o.getId());
                    m.put("orderId", o.getOrder().getId());
                    m.put("productName", o.getOrder().getListing().getProductName());
                    m.put("quantity", o.getOrder().getRequestedQuantity() + " " + o.getOrder().getUnit());
                    m.put("pickupDistrict", o.getOrder().getListing().getDistrict());
                    m.put("pickupState", o.getOrder().getListing().getState());
                    m.put("deliveryDistrict", o.getOrder().getDeliveryDistrict());
                    m.put("deliveryState", o.getOrder().getDeliveryState());
                    m.put("deliveryAddress", o.getOrder().getDeliveryAddress());
                    m.put("distanceKm", o.getDistanceKm());
                    m.put("expiresAt", o.getOfferExpiresAt());
                    return m;
                }).collect(Collectors.toList())
        );
    }

    @PostMapping("/accept/{offerId}")
    public ResponseEntity<Map<String, String>> acceptOffer(@PathVariable Long offerId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        DeliveryProfile profile = deliveryProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Delivery profile not found"));
        deliveryMatchingService.acceptOffer(offerId, profile.getId());
        return ResponseEntity.ok(Map.of("message", "Offer accepted"));
    }

    @PostMapping("/decline/{offerId}")
    public ResponseEntity<Map<String, String>> declineOffer(@PathVariable Long offerId,
            @RequestParam(required = false) String reason, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        DeliveryProfile profile = deliveryProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Delivery profile not found"));
        deliveryMatchingService.declineOffer(offerId, profile.getId(), reason);
        return ResponseEntity.ok(Map.of("message", "Offer declined"));
    }

    @GetMapping("/my-deliveries")
    public ResponseEntity<List<Map<String, Object>>> myDeliveries(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        DeliveryProfile profile = deliveryProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Delivery profile not found"));
        return ResponseEntity.ok(
            deliveryOfferRepository.findActiveDeliveriesByProfile(profile.getId()).stream().map(o -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("offerId", o.getId());
                m.put("orderId", o.getOrder().getId());
                m.put("productName", o.getOrder().getListing().getProductName());
                m.put("buyerName", o.getOrder().getBuyer().getFullName());
                m.put("buyerMobile", o.getOrder().getBuyer().getMobileNumber());
                m.put("sellerName", o.getOrder().getSeller().getFullName());
                m.put("sellerMobile", o.getOrder().getSeller().getMobileNumber());
                m.put("pickupAddress", o.getOrder().getListing().getVillage() + ", " + o.getOrder().getListing().getDistrict());
                m.put("deliveryAddress", o.getOrder().getDeliveryAddress());
                m.put("orderStatus", o.getOrder().getStatus());
                return m;
            }).collect(Collectors.toList())
        );
    }

    @PostMapping("/update-status/{orderId}")
    public ResponseEntity<Map<String, String>> updateStatus(@PathVariable Long orderId,
            @RequestParam Order.OrderStatus status, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        DeliveryProfile profile = deliveryProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Delivery profile not found"));
        deliveryMatchingService.updateDeliveryStatus(orderId, status, profile.getId());
        return ResponseEntity.ok(Map.of("message", "Status updated to " + status));
    }

    @PostMapping("/confirm-delivered/{orderId}")
    public ResponseEntity<Map<String, String>> confirmDelivered(@PathVariable Long orderId,
            @RequestParam String otp, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        DeliveryProfile profile = deliveryProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Delivery profile not found"));
        deliveryOfferRepository.findByOrderIdAndStatus(orderId, DeliveryOffer.OfferStatus.ACCEPTED)
            .filter(o -> o.getDeliveryProfile().getId().equals(profile.getId()))
            .orElseThrow(() -> new ApiException("Not authorized for this delivery", 403));
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!otp.equals(order.getDeliveryOtp())) throw new ApiException("Invalid OTP", 400);
        order.setStatus(Order.OrderStatus.DELIVERED);
        order.setDeliveredAt(java.time.LocalDateTime.now());
        orderRepository.save(order);
        profile.setCompletedDeliveries(profile.getCompletedDeliveries() + 1);
        deliveryProfileRepository.save(profile);
        return ResponseEntity.ok(Map.of("message", "Delivery confirmed"));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
