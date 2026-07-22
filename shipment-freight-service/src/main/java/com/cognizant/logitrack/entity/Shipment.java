package com.cognizant.logitrack.entity;

import com.cognizant.logitrack.enums.ShipmentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shipments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer shipmentId;

    @ManyToOne
    @JoinColumn(name = "FreightOrderID")
    private FreightOrder freightOrder;

    private Integer carrierId;

    private Integer vehicleId;

    private Integer driverId;

    private Integer rateCardId;

    private BigDecimal freightCost;

    private LocalDate dispatchDate;

    private LocalDate estimatedArrival;

    private LocalDate actualArrival;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.PLANNED;
}