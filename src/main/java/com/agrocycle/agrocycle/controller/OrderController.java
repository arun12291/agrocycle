package com.agrocycle.agrocycle.controller;

import com.agrocycle.agrocycle.dto.OrderDto;
import com.agrocycle.agrocycle.dto.OrderRequest;
import com.agrocycle.agrocycle.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderRequest req, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(201).body(orderService.createOrder(req, user.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(orderService.getOrder(id, user.getUsername()));
    }

    @GetMapping("/my/buying")
    public ResponseEntity<List<OrderDto>> myBuying(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(orderService.getMyBuyerOrders(user.getUsername()));
    }

    @GetMapping("/my/selling")
    public ResponseEntity<List<OrderDto>> mySelling(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(orderService.getMySellerOrders(user.getUsername()));
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<OrderDto> respond(@PathVariable Long id, @RequestParam boolean accept,
            @RequestParam(required = false) String reason, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(orderService.respondToOrder(id, accept, reason, user.getUsername()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancel(@PathVariable Long id, @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(orderService.cancelOrder(id, reason, user.getUsername()));
    }

    @PostMapping("/{id}/confirm-delivery")
    public ResponseEntity<OrderDto> confirmDelivery(@PathVariable Long id, @RequestParam String otp,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(orderService.confirmDelivery(id, otp, user.getUsername()));
    }

    @PostMapping("/{id}/confirm-receipt")
    public ResponseEntity<OrderDto> confirmReceipt(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(orderService.confirmReceipt(id, user.getUsername()));
    }
}
