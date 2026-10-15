package com.agrocycle.agrocycle.repository;

import com.agrocycle.agrocycle.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByOrderIdAndReviewerId(Long orderId, Long reviewerId);
    Optional<Review> findByOrderIdAndReviewerId(Long orderId, Long reviewerId);
    List<Review> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.seller.id = :sellerId")
    Double getAverageRatingBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.seller.id = :sellerId")
    Long countBySeller(@Param("sellerId") Long sellerId);
}
