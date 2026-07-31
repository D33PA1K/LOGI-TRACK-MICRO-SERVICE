package com.cognizant.logitrack.entity;

import com.cognizant.logitrack.enums.FreightOrderStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "freight_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreightOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer freightOrderId;

    private Integer shipperId;
    private Integer poId;

    private Integer originLocationId;

    private Integer destinationLocationId;

    private Integer routeId;

    @Column(columnDefinition = "TEXT")
    private String cargoDescription;

    private BigDecimal weight;

    private BigDecimal volume;

    private LocalDate requiredDeliveryDate;

    // Automatically set to the current date when the freight order is first
    // persisted; not updatable thereafter.
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDate dateOfCreation;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FreightOrderStatus status = FreightOrderStatus.DRAFT;
}
