package com.cognizant.logitrack.entity;

import com.cognizant.logitrack.enums.RateCardStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rate_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rateCardId;
    @ManyToOne
    @JoinColumn(name = "CarrierID")
    private Carrier carrier;
    @ManyToOne
    @JoinColumn(name = "RouteID")
    private Route route;
    private BigDecimal baseRate;
    @Column(length = 100)
    private String weightSlab;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RateCardStatus status = RateCardStatus.ACTIVE;
}
