package com.agrocycle.agrocycle.controller;

import com.agrocycle.agrocycle.entity.*;
import com.agrocycle.agrocycle.exception.*;
import com.agrocycle.agrocycle.repository.*;
import com.agrocycle.agrocycle.dto.ReviewRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ReviewController(ReviewRepository reviewRepository, OrderRepository orderRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createReview(@Valid @RequestBody ReviewRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        User reviewer = getUser(userDetails.getUsername());
        Order order = orderRepository.findById(req.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getBuyer().getId().equals(reviewer.getId())) throw new ApiException("Not authorized", 403);
        if (order.getStatus() != Order.OrderStatus.COMPLETED) throw new ApiException("Order not completed", 400);
        if (reviewRepository.existsByOrderIdAndReviewerId(order.getId(), reviewer.getId()))
            throw new ApiException("Already reviewed", 409);
        Review review = new Review();
        review.setOrder(order);
        review.setReviewer(reviewer);
        review.setSeller(order.getSeller());
        review.setRating(req.getRating());
        review.setComment(req.getComment());
        Review saved = reviewRepository.save(review);
        return ResponseEntity.status(201).body(Map.of("id", saved.getId(), "message", "Review submitted"));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Map<String, Object>>> sellerReviews(@PathVariable Long sellerId) {
        return ResponseEntity.ok(
            reviewRepository.findBySellerIdOrderByCreatedAtDesc(sellerId).stream().map(r -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", r.getId());
                m.put("reviewerName", r.getReviewer().getFullName());
                m.put("rating", r.getRating());
                m.put("comment", r.getComment());
                m.put("createdAt", r.getCreatedAt());
                return m;
            }).collect(Collectors.toList())
        );
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
