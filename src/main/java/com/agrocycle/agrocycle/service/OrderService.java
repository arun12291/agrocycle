package com.agrocycle.agrocycle.service;

import com.agrocycle.agrocycle.dto.*;
import com.agrocycle.agrocycle.entity.*;
import com.agrocycle.agrocycle.exception.*;
import com.agrocycle.agrocycle.notification.NotificationService;
import com.agrocycle.agrocycle.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OrderRepository orderRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final NotificationService notificationService;
    private final DeliveryMatchingService deliveryMatchingService;
    private final ReviewRepository reviewRepository;

    public OrderService(OrderRepository orderRepository, ListingRepository listingRepository,
                        UserRepository userRepository, OrderStatusHistoryRepository statusHistoryRepository,
                        NotificationService notificationService, DeliveryMatchingService deliveryMatchingService,
                        ReviewRepository reviewRepository) {
        this.orderRepository = orderRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.notificationService = notificationService;
        this.deliveryMatchingService = deliveryMatchingService;
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public OrderDto createOrder(OrderRequest req, String buyerEmail) {
        User buyer = userRepository.findByEmail(buyerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Listing listing = listingRepository.findById(req.getListingId())
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
        if (listing.getStatus() != Listing.ListingStatus.ACTIVE)
            throw new ApiException("Listing is not available", 400);
        if (listing.getSeller().getId().equals(buyer.getId()))
            throw new ApiException("You cannot order from your own listing", 400);
        orderRepository.findActiveBuyerOrderForListing(buyer.getId(), listing.getId())
            .ifPresent(o -> { throw new ApiException("You already have an active order for this listing", 409); });

        BigDecimal available = listing.getAvailableQuantity().subtract(listing.getReservedQuantity());
        if (req.getRequestedQuantity().compareTo(available) > 0)
            throw new ApiException("Requested quantity exceeds available: " + available + " " + listing.getUnit(), 400);

        try {
            listing.setReservedQuantity(listing.getReservedQuantity().add(req.getRequestedQuantity()));
            listingRepository.save(listing);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ApiException("Listing was updated concurrently. Please try again.", 409);
        }

        Order order = new Order();
        order.setListing(listing);
        order.setBuyer(buyer);
        order.setSeller(listing.getSeller());
        order.setRequestedQuantity(req.getRequestedQuantity());
        order.setUnit(listing.getUnit());
        order.setPricePerUnit(listing.getPricePerUnit());
        order.setTotalAmount(listing.getPricePerUnit().multiply(req.getRequestedQuantity()));
        order.setDeliveryAddress(req.getDeliveryAddress());
        order.setDeliveryVillage(req.getDeliveryVillage());
        order.setDeliveryDistrict(req.getDeliveryDistrict());
        order.setDeliveryState(req.getDeliveryState());
        order.setDeliveryPostalCode(req.getDeliveryPostalCode());
        order.setDeliveryLatitude(req.getDeliveryLatitude());
        order.setDeliveryLongitude(req.getDeliveryLongitude());
        order.setStatus(Order.OrderStatus.PENDING_SELLER);
        Order saved = orderRepository.save(order);
        recordStatusChange(saved, null, Order.OrderStatus.PENDING_SELLER, buyer.getId(), "FARMER_BUYER", "Order placed");

        notificationService.sendNotification(listing.getSeller(), Notification.NotificationType.NEW_ORDER_REQUEST,
            "New Order Request",
            buyer.getFullName() + " requested " + req.getRequestedQuantity() + " " + listing.getUnit() +
                " of " + listing.getProductName() + ". Please review and respond.",
            saved.getId(), listing.getId());

        log.info("Order {} created by buyer {}", saved.getId(), buyer.getEmail());
        return OrderDto.from(saved, false, false);
    }

    @Transactional
    public OrderDto respondToOrder(Long orderId, boolean accept, String reason, String sellerEmail) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getSeller().getEmail().equals(sellerEmail))
            throw new ApiException("Not authorized", 403);
        if (order.getStatus() != Order.OrderStatus.PENDING_SELLER)
            throw new ApiException("Order is not pending seller response", 400);

        Listing listing = order.getListing();
        if (accept) {
            order.setStatus(Order.OrderStatus.ACCEPTED);
            recordStatusChange(order, Order.OrderStatus.PENDING_SELLER, Order.OrderStatus.ACCEPTED,
                order.getSeller().getId(), "FARMER_BUYER", "Seller accepted");
            listing.setAvailableQuantity(listing.getAvailableQuantity().subtract(order.getRequestedQuantity()));
            listing.setReservedQuantity(listing.getReservedQuantity().subtract(order.getRequestedQuantity()));
            if (listing.getAvailableQuantity().compareTo(BigDecimal.ZERO) <= 0)
                listing.setStatus(Listing.ListingStatus.SOLD_OUT);
            listingRepository.save(listing);
            order.setDeliveryOtp(String.format("%06d", SECURE_RANDOM.nextInt(1000000)));
            notificationService.sendNotification(order.getBuyer(), Notification.NotificationType.ORDER_ACCEPTED,
                "Order Accepted!", order.getSeller().getFullName() + " accepted your order for " +
                    listing.getProductName() + ". Finding a delivery person...",
                order.getId(), listing.getId());
            orderRepository.save(order);
            order.setStatus(Order.OrderStatus.SEARCHING_DELIVERY);
            recordStatusChange(order, Order.OrderStatus.ACCEPTED, Order.OrderStatus.SEARCHING_DELIVERY,
                null, "SYSTEM", "Searching for delivery");
            orderRepository.save(order);
            deliveryMatchingService.findAndOfferDelivery(order);
        } else {
            listing.setReservedQuantity(listing.getReservedQuantity().subtract(order.getRequestedQuantity()));
            listingRepository.save(listing);
            order.setStatus(Order.OrderStatus.REJECTED);
            order.setStatusReason(reason);
            recordStatusChange(order, Order.OrderStatus.PENDING_SELLER, Order.OrderStatus.REJECTED,
                order.getSeller().getId(), "FARMER_BUYER", reason);
            notificationService.sendNotification(order.getBuyer(), Notification.NotificationType.ORDER_REJECTED,
                "Order Rejected", order.getSeller().getFullName() + " rejected your order for " +
                    listing.getProductName() + (reason != null ? ". Reason: " + reason : "."),
                order.getId(), listing.getId());
        }
        return OrderDto.from(orderRepository.save(order), true, false);
    }

    @Transactional
    public OrderDto cancelOrder(Long orderId, String reason, String userEmail) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean isBuyer = order.getBuyer().getId().equals(user.getId());
        boolean isSeller = order.getSeller().getId().equals(user.getId());
        if (!isBuyer && !isSeller) throw new ApiException("Not authorized", 403);
        if (order.getStatus() == Order.OrderStatus.PICKED_UP || order.getStatus() == Order.OrderStatus.IN_TRANSIT ||
            order.getStatus() == Order.OrderStatus.DELIVERED || order.getStatus() == Order.OrderStatus.COMPLETED)
            throw new ApiException("Cannot cancel in status: " + order.getStatus(), 400);
        if (order.getStatus() == Order.OrderStatus.PENDING_SELLER) {
            Listing listing = order.getListing();
            listing.setReservedQuantity(listing.getReservedQuantity().subtract(order.getRequestedQuantity()));
            listingRepository.save(listing);
        }
        Order.OrderStatus prev = order.getStatus();
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setStatusReason(reason);
        recordStatusChange(order, prev, Order.OrderStatus.CANCELLED, user.getId(), user.getRole().name(), reason);
        User toNotify = isBuyer ? order.getSeller() : order.getBuyer();
        notificationService.sendNotification(toNotify, Notification.NotificationType.ORDER_CANCELLED,
            "Order Cancelled", (isBuyer ? "Buyer" : "Seller") + " cancelled the order for " +
                order.getListing().getProductName() + (reason != null ? ". Reason: " + reason : "."),
            order.getId(), order.getListing().getId());
        return OrderDto.from(orderRepository.save(order), false, false);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getMyBuyerOrders(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(user.getId())
            .stream().map(o -> OrderDto.from(o, false, o.getStatus() == Order.OrderStatus.DELIVERY_ACCEPTED))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getMySellerOrders(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findBySellerIdOrderByCreatedAtDesc(user.getId())
            .stream().map(o -> OrderDto.from(o, false, false)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean isBuyer = order.getBuyer().getId().equals(user.getId());
        boolean isSeller = order.getSeller().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        if (!isBuyer && !isSeller && !isAdmin) throw new ApiException("Not authorized", 403);
        boolean showOtp = isBuyer && (order.getStatus() == Order.OrderStatus.DELIVERY_ACCEPTED ||
            order.getStatus() == Order.OrderStatus.PICKED_UP || order.getStatus() == Order.OrderStatus.IN_TRANSIT);
        return OrderDto.from(order, isSeller || isAdmin, showOtp);
    }

    @Transactional
    public OrderDto confirmDelivery(Long orderId, String otp, String deliveryPersonEmail) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getStatus() != Order.OrderStatus.IN_TRANSIT && order.getStatus() != Order.OrderStatus.PICKED_UP)
            throw new ApiException("Order is not in deliverable state", 400);
        if (!otp.equals(order.getDeliveryOtp())) throw new ApiException("Invalid delivery OTP", 400);
        order.setStatus(Order.OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        recordStatusChange(order, Order.OrderStatus.IN_TRANSIT, Order.OrderStatus.DELIVERED, null, "DELIVERY_PERSON", "Delivery confirmed with OTP");
        notificationService.sendNotification(order.getBuyer(), Notification.NotificationType.ORDER_DELIVERED,
            "Order Delivered!", "Your order for " + order.getListing().getProductName() + " has been delivered. Please confirm receipt.",
            order.getId(), order.getListing().getId());
        notificationService.sendNotification(order.getSeller(), Notification.NotificationType.ORDER_DELIVERED,
            "Order Delivered", "Order #" + order.getId() + " has been delivered.", order.getId(), order.getListing().getId());
        return OrderDto.from(orderRepository.save(order), false, false);
    }

    @Transactional
    public OrderDto confirmReceipt(Long orderId, String buyerEmail) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getBuyer().getEmail().equals(buyerEmail)) throw new ApiException("Not authorized", 403);
        if (order.getStatus() != Order.OrderStatus.DELIVERED) throw new ApiException("Order has not been delivered yet", 400);
        order.setStatus(Order.OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        recordStatusChange(order, Order.OrderStatus.DELIVERED, Order.OrderStatus.COMPLETED, order.getBuyer().getId(), "FARMER_BUYER", "Buyer confirmed receipt");
        notificationService.sendNotification(order.getSeller(), Notification.NotificationType.ORDER_COMPLETED,
            "Order Completed", "Order #" + order.getId() + " completed! Buyer confirmed receipt.", order.getId(), order.getListing().getId());
        return OrderDto.from(orderRepository.save(order), false, false);
    }

    private void recordStatusChange(Order order, Order.OrderStatus from, Order.OrderStatus to,
                                     Long changedBy, String changedByRole, String notes) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setChangedByUserId(changedBy);
        history.setChangedByRole(changedByRole);
        history.setNotes(notes);
        statusHistoryRepository.save(history);
    }
}
