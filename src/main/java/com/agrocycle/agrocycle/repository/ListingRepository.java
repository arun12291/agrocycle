package com.agrocycle.agrocycle.repository;

import com.agrocycle.agrocycle.entity.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE' " +
           "AND (:type IS NULL OR l.listingType = :type) " +
           "AND (:category IS NULL OR l.category = :category) " +
           "AND (:state IS NULL OR LOWER(l.state) = LOWER(:state)) " +
           "AND (:district IS NULL OR LOWER(l.district) LIKE LOWER(CONCAT('%', :district, '%'))) " +
           "AND (:minPrice IS NULL OR l.pricePerUnit >= :minPrice) " +
           "AND (:maxPrice IS NULL OR l.pricePerUnit <= :maxPrice) " +
           "AND (:minQty IS NULL OR l.availableQuantity >= :minQty) " +
           "AND (:keyword IS NULL OR LOWER(l.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Listing> searchListings(
        @Param("type") Listing.ListingType type,
        @Param("category") Listing.Category category,
        @Param("state") String state,
        @Param("district") String district,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("minQty") BigDecimal minQty,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    List<Listing> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE' ORDER BY l.createdAt DESC")
    List<Listing> findFeaturedListings(Pageable pageable);

    Long countByStatus(Listing.ListingStatus status);

    @Query("SELECT SUM(l.totalQuantity - l.availableQuantity) FROM Listing l " +
           "WHERE l.status IN ('ACTIVE', 'SOLD_OUT')")
    BigDecimal totalMaterialTraded();
}
