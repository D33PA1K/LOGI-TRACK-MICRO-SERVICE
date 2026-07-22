package com.cognizant.logitrack.repository;

import com.cognizant.logitrack.entity.RateCard;
import com.cognizant.logitrack.enums.RateCardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RateCardRepository extends JpaRepository<RateCard, Integer> {

    List<RateCard> findByCarrier_CarrierId(Integer carrierId);

    List<RateCard> findByRoute_RouteId(Integer routeId);

    List<RateCard> findByStatus(RateCardStatus status);

    boolean existsByCarrier_CarrierIdAndRoute_RouteIdAndStatus(
            Integer carrierId,
            Integer routeId,
            RateCardStatus status
    );

    @Query("""
           SELECT rc
           FROM RateCard rc
           WHERE rc.carrier.carrierId = :carrierId
             AND rc.route.routeId = :routeId
             AND rc.status = :status
             AND (rc.effectiveDate IS NULL OR rc.effectiveDate <= :shipmentDate)
             AND (rc.expiryDate IS NULL OR rc.expiryDate >= :shipmentDate)
           """)
    Optional<RateCard> findValidRateCard(
            @Param("carrierId") Integer carrierId,
            @Param("routeId") Integer routeId,
            @Param("status") RateCardStatus status,
            @Param("shipmentDate") LocalDate shipmentDate
    );
}