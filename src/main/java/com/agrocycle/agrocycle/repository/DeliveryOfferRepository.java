package com.agrocycle.agrocycle.repository;

import com.agrocycle.agrocycle.entity.DeliveryOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryOfferRepository extends JpaRepository<DeliveryOffer, Long> {

    List<DeliveryOffer> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    List<DeliveryOffer> findByDeliveryProfileIdAndStatus(Long deliveryProfileId, DeliveryOffer.OfferStatus status);

    Optional<DeliveryOffer> findByOrderIdAndStatus(Long orderId, DeliveryOffer.OfferStatus status);

    @Query("SELECT do FROM DeliveryOffer do WHERE do.deliveryProfile.id = :profileId " +
           "AND do.status = 'ACCEPTED' AND do.order.status NOT IN ('COMPLETED', 'CANCELLED', 'DELIVERY_FAILED')")
    List<DeliveryOffer> findActiveDeliveriesByProfile(@Param("profileId") Long profileId);

    @Query("SELECT COUNT(do) FROM DeliveryOffer do WHERE do.deliveryProfile.id = :profileId " +
           "AND do.status = 'ACCEPTED' AND do.order.status NOT IN ('COMPLETED', 'CANCELLED', 'DELIVERY_FAILED')")
    Long countActiveDeliveriesByProfile(@Param("profileId") Long profileId);

    @Query("SELECT do FROM DeliveryOffer do WHERE do.order.id = :orderId AND do.deliveryProfile.id = :profileId")
    Optional<DeliveryOffer> findByOrderIdAndDeliveryProfileId(@Param("orderId") Long orderId, @Param("profileId") Long profileId);
}
