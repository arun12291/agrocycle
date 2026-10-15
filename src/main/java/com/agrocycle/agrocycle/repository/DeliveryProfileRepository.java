package com.agrocycle.agrocycle.repository;

import com.agrocycle.agrocycle.entity.DeliveryProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryProfileRepository extends JpaRepository<DeliveryProfile, Long> {
    Optional<DeliveryProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    List<DeliveryProfile> findByApprovalStatus(DeliveryProfile.ApprovalStatus status);
    
    @Query("SELECT dp FROM DeliveryProfile dp WHERE dp.approvalStatus = 'APPROVED' AND dp.available = true")
    List<DeliveryProfile> findAvailableApprovedDeliveryPersons();

    @Query("SELECT dp FROM DeliveryProfile dp WHERE dp.approvalStatus = 'APPROVED' AND dp.available = true " +
           "AND (dp.serviceArea LIKE %:district% OR dp.serviceArea LIKE %:state%)")
    List<DeliveryProfile> findAvailableByServiceArea(@Param("district") String district, @Param("state") String state);
}
