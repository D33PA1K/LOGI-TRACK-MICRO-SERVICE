package com.cognizant.logitrack.entity;

import com.cognizant.logitrack.enums.POStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purchase_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer poId;
    @ManyToOne
    @JoinColumn(name = "SupplierID")
    private Supplier supplier;
    private Integer warehouseId;
    @Column(columnDefinition = "TEXT")
    private String lineItems;
    private BigDecimal totalValue;
    private LocalDate orderDate;
    private LocalDate expectedDelivery;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private POStatus status = POStatus.DRAFT;
}
