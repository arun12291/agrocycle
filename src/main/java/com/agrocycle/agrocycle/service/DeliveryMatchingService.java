package com.agrocycle.agrocycle.service;

import com.agrocycle.agrocycle.entity.*;
import com.agrocycle.agrocycle.notification.NotificationService;
import com.agrocycle.agrocycle.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeliveryMatchingService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryMatchingService.class);

    private final DeliveryProfileRepository deliveryProfileRepository;
    private final DeliveryOfferRepository deliveryOfferRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final NotificationService notificationService;

    @Value("${agrocycle.delivery.offer-timeout-minutes:30}")
    private int offerTimeoutMinutes;

    @Value("${agrocycle.delivery.max-candidates:5}")
    private int maxCandidates;

    public DeliveryMatchingService(DeliveryProfileRepository deliveryProfileRepository,
                                   DeliveryOfferRepository deliveryOfferRepository,
                                   OrderRepository orderRepository,
                                   OrderStatusHistoryRepository statusHistoryRepository,
                                   NotificationService notificationService) {
        this.deliveryProfileRepository = deliveryProfileRepository;
        this.deliveryOfferRepository = deliveryOfferRepository;
        this.orderRepository = orderRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void findAndOfferDelivery(Order order) {
        log.info("Starting delivery matching for order {}", order.getId());
        List<DeliveryProfile> candidates = deliveryProfileRepository.findAvailableApprovedDeliveryPersons();
        Set<Long> alreadyOffered = deliveryOfferRepository.findByOrderIdOrderByCreatedAtDesc(order.getId())
            .stream().filter(o -> o.getStatus() != DeliveryOffer.OfferStatus.CANCELLED)
            .map(o -> o.getDeliveryProfile().getId()).collect(Collectors.toSet());
        final List<DeliveryProfile> finalCandidates = candidates.stream()
            .filter(dp -> !alreadyOffered.contains(dp.getId())).collect(Collectors.toList());
        if (finalCandidates.isEmpty()) {
            log.warn("No available delivery persons for order {}", order.getId());
            order.setStatus(Order.OrderStatus.DELIVERY_PENDING);
            recordStatus(order, Order.OrderStatus.SEARCHING_DELIVERY, Order.OrderStatus.DELIVERY_PENDING, "SYSTEM", "No available delivery persons");
            orderRepository.save(order);
            notifyNoDelivery(order);
            return;
        }
        finalCandidates.stream()
            .map(dp -> new double[]{dp.getId(), scoreCandidate(dp, order)})
            .sorted((a, b) -> Double.compare(b[1], a[1]))
            .limit(maxCandidates)
            .findFirst()
            .ifPresent(best -> {
                DeliveryProfile bestProfile = finalCandidates.stream()
                    .filter(dp -> dp.getId() == (long) best[0]).findFirst().orElse(finalCandidates.get(0));
                offerToDeliveryPerson(order, bestProfile, best[1]);
            });
    }

    private void offerToDeliveryPerson(Order order, DeliveryProfile profile, double score) {
        DeliveryOffer offer = new DeliveryOffer();
        offer.setOrder(order);
        offer.setDeliveryProfile(profile);
        offer.setStatus(DeliveryOffer.OfferStatus.PENDING);
        offer.setScore(score);
        offer.setOfferExpiresAt(LocalDateTime.now().plusMinutes(offerTimeoutMinutes));
        offer.setDistanceKm(estimateDistance(profile, order));
        deliveryOfferRepository.save(offer);
        notificationService.sendNotification(profile.getUser(), Notification.NotificationType.DELIVERY_OFFER,
            "New Delivery Offer",
            "Order #" + order.getId() + " — Pickup: " + order.getListing().getVillage() + ", " +
                order.getListing().getDistrict() + " → Delivery: " + order.getDeliveryDistrict() +
                ", " + order.getDeliveryState() + ". Respond within " + offerTimeoutMinutes + " minutes.",
            order.getId(), null);
        log.info("Offer sent to {} for order {} (score={})", profile.getUser().getEmail(), order.getId(), score);
    }

    private double scoreCandidate(DeliveryProfile dp, Order order) {
        double score = 0;
        String pickupDistrict = order.getListing().getDistrict().toLowerCase();
        String pickupState = order.getListing().getState().toLowerCase();
        String delivDistrict = order.getDeliveryDistrict() != null ? order.getDeliveryDistrict().toLowerCase() : "";
        String delivState = order.getDeliveryState() != null ? order.getDeliveryState().toLowerCase() : "";
        String serviceArea = dp.getServiceArea().toLowerCase();
        if (serviceArea.contains(pickupDistrict) || serviceArea.contains(delivDistrict)) score += 40;
        else if (serviceArea.contains(pickupState) || serviceArea.contains(delivState)) score += 20;
        if (dp.getLatitude() != null && dp.getLongitude() != null &&
            order.getListing().getLatitude() != null && order.getListing().getLongitude() != null) {
            double dist = haversineKm(dp.getLatitude(), dp.getLongitude(),
                order.getListing().getLatitude(), order.getListing().getLongitude());
            score += Math.max(0, 30 - (dist / 200.0 * 30));
        }
        long active = deliveryOfferRepository.countActiveDeliveriesByProfile(dp.getId());
        if (active == 0) score += 15; else if (active == 1) score += 8;
        if (dp.getCarryingCapacityKg() != null) {
            if (dp.getCarryingCapacityKg() >= order.getRequestedQuantity().doubleValue()) score += 5;
        } else score += 3;
        if (dp.getRatingCount() != null && dp.getRatingCount() > 0 && dp.getAverageRating() != null)
            score += Math.min(dp.getAverageRating() * 2, 10);
        return score;
    }

    private Double estimateDistance(DeliveryProfile dp, Order order) {
        if (dp.getLatitude() != null && dp.getLongitude() != null &&
            order.getListing().getLatitude() != null && order.getListing().getLongitude() != null)
            return haversineKm(dp.getLatitude(), dp.getLongitude(),
                order.getListing().getLatitude(), order.getListing().getLongitude());
        return null;
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2) +
            Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.sin(dLon/2)*Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    @Transactional
    public void acceptOffer(Long offerId, Long deliveryProfileId) {
        DeliveryOffer offer = deliveryOfferRepository.findById(offerId)
            .orElseThrow(() -> new RuntimeException("Offer not found"));
        if (!offer.getDeliveryProfile().getId().equals(deliveryProfileId)) throw new RuntimeException("Not authorized");
        if (offer.getStatus() != DeliveryOffer.OfferStatus.PENDING) throw new RuntimeException("Offer is not pending");
        if (offer.getOfferExpiresAt().isBefore(LocalDateTime.now())) {
            offer.setStatus(DeliveryOffer.OfferStatus.EXPIRED);
            deliveryOfferRepository.save(offer);
            throw new RuntimeException("Offer has expired");
        }
        Order order = offer.getOrder();
        if (order.getStatus() != Order.OrderStatus.SEARCHING_DELIVERY && order.getStatus() != Order.OrderStatus.DELIVERY_PENDING)
            throw new RuntimeException("Order no longer available");
        offer.setStatus(DeliveryOffer.OfferStatus.ACCEPTED);
        offer.setRespondedAt(LocalDateTime.now());
        deliveryOfferRepository.save(offer);
        deliveryOfferRepository.findByOrderIdOrderByCreatedAtDesc(order.getId()).stream()
            .filter(o -> o.getStatus() == DeliveryOffer.OfferStatus.PENDING && !o.getId().equals(offerId))
            .forEach(o -> { o.setStatus(DeliveryOffer.OfferStatus.CANCELLED); deliveryOfferRepository.save(o); });
        order.setStatus(Order.OrderStatus.DELIVERY_ACCEPTED);
        recordStatus(order, Order.OrderStatus.SEARCHING_DELIVERY, Order.OrderStatus.DELIVERY_ACCEPTED,
            "SYSTEM", "Delivery person accepted: " + offer.getDeliveryProfile().getUser().getFullName());
        orderRepository.save(order);
        String dpName = offer.getDeliveryProfile().getUser().getFullName();
        notificationService.sendNotification(order.getBuyer(), Notification.NotificationType.DELIVERY_ACCEPTED,
            "Delivery Person Assigned", dpName + " will deliver your Order #" + order.getId(), order.getId(), null);
        notificationService.sendNotification(order.getSeller(), Notification.NotificationType.DELIVERY_ACCEPTED,
            "Delivery Person Assigned", dpName + " accepted delivery for Order #" + order.getId(), order.getId(), null);
    }

    @Transactional
    public void declineOffer(Long offerId, Long deliveryProfileId, String reason) {
        DeliveryOffer offer = deliveryOfferRepository.findById(offerId)
            .orElseThrow(() -> new RuntimeException("Offer not found"));
        if (!offer.getDeliveryProfile().getId().equals(deliveryProfileId)) throw new RuntimeException("Not authorized");
        if (offer.getStatus() != DeliveryOffer.OfferStatus.PENDING) throw new RuntimeException("Offer is not pending");
        offer.setStatus(DeliveryOffer.OfferStatus.DECLINED);
        offer.setDeclineReason(reason);
        offer.setRespondedAt(LocalDateTime.now());
        deliveryOfferRepository.save(offer);
        findAndOfferDelivery(offer.getOrder());
    }

    @Transactional
    public void updateDeliveryStatus(Long orderId, Order.OrderStatus newStatus, Long deliveryProfileId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        DeliveryOffer accepted = deliveryOfferRepository.findByOrderIdAndStatus(orderId, DeliveryOffer.OfferStatus.ACCEPTED)
            .orElseThrow(() -> new RuntimeException("No accepted offer for this order"));
        if (!accepted.getDeliveryProfile().getId().equals(deliveryProfileId)) throw new RuntimeException("Not authorized");
        Order.OrderStatus current = order.getStatus();
        boolean valid = (newStatus == Order.OrderStatus.PICKED_UP && current == Order.OrderStatus.DELIVERY_ACCEPTED) ||
                        (newStatus == Order.OrderStatus.IN_TRANSIT && current == Order.OrderStatus.PICKED_UP);
        if (!valid) throw new RuntimeException("Invalid transition: " + current + " → " + newStatus);
        Order.OrderStatus prev = order.getStatus();
        order.setStatus(newStatus);
        recordStatus(order, prev, newStatus, "DELIVERY_PERSON", null);
        orderRepository.save(order);
        Notification.NotificationType notifType = newStatus == Order.OrderStatus.PICKED_UP ?
            Notification.NotificationType.ORDER_PICKED_UP : Notification.NotificationType.ORDER_IN_TRANSIT;
        String msg = newStatus == Order.OrderStatus.PICKED_UP ? "Your order has been picked up!" : "Your order is in transit!";
        notificationService.sendNotification(order.getBuyer(), notifType, "Delivery Update", msg, order.getId(), null);
        notificationService.sendNotification(order.getSeller(), notifType, "Delivery Update",
            "Order #" + orderId + ": " + newStatus, order.getId(), null);
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void expirePendingOffers() {
        deliveryOfferRepository.findAll().stream()
            .filter(o -> o.getStatus() == DeliveryOffer.OfferStatus.PENDING &&
                o.getOfferExpiresAt() != null && o.getOfferExpiresAt().isBefore(LocalDateTime.now()))
            .forEach(offer -> {
                offer.setStatus(DeliveryOffer.OfferStatus.EXPIRED);
                deliveryOfferRepository.save(offer);
                Order order = offer.getOrder();
                if (order.getStatus() == Order.OrderStatus.SEARCHING_DELIVERY ||
                    order.getStatus() == Order.OrderStatus.DELIVERY_PENDING)
                    findAndOfferDelivery(order);
            });
    }

    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void retryDeliveryMatching() {
        orderRepository.findOrdersNeedingDelivery().forEach(this::findAndOfferDelivery);
    }

    private void notifyNoDelivery(Order order) {
        String msg = "No delivery person available for Order #" + order.getId() + ". We will retry automatically.";
        notificationService.sendNotification(order.getBuyer(), Notification.NotificationType.SYSTEM_ALERT,
            "Delivery Search", msg, order.getId(), null);
        notificationService.sendNotification(order.getSeller(), Notification.NotificationType.SYSTEM_ALERT,
            "Delivery Search", msg, order.getId(), null);
    }

    private void recordStatus(Order order, Order.OrderStatus from, Order.OrderStatus to, String role, String notes) {
        OrderStatusHistory h = new OrderStatusHistory();
        h.setOrder(order); h.setFromStatus(from); h.setToStatus(to);
        h.setChangedByRole(role); h.setNotes(notes);
        statusHistoryRepository.save(h);
    }
}
