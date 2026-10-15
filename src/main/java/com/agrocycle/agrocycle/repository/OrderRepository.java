package com.agrocycle.agrocycle.repository;

import com.agrocycle.agrocycle.entity.Order;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
    List<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    List<Order> findBySellerIdAndStatusOrderByCreatedAtDesc(Long sellerId, Order.OrderStatus status);
    List<Order> findByStatus(Order.OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") Order.OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.requestedQuantity), 0) FROM Order o WHERE o.status = 'COMPLETED'")
    BigDecimal sumCompletedQuantity();

    @Query("SELECT o FROM Order o WHERE o.buyer.id = :buyerId AND o.listing.id = :listingId " +
           "AND o.status NOT IN ('REJECTED', 'CANCELLED', 'DELIVERY_FAILED')")
    Optional<Order> findActiveBuyerOrderForListing(@Param("buyerId") Long buyerId, @Param("listingId") Long listingId);

    @Query("SELECT o FROM Order o WHERE o.status IN ('SEARCHING_DELIVERY', 'DELIVERY_PENDING')")
    List<Order> findOrdersNeedingDelivery();

    List<Order> findByListingId(Long listingId);
}
