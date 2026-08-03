package com.cognizant.logitrack.entity;

import com.cognizant.logitrack.enums.WarehouseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "warehouses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer warehouseId;
    @Column(nullable = false)
    private String warehouseName;
    @Column(nullable = false)
    private String addressLine;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String state;
    @Column(nullable = false)
    private String country;
    // Mandatory operational details so the warehouse can actually be used as a
    // stocking/despatch location, not just a label.
    private String postalCode;
    private String contactNumber;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private WarehouseStatus status = WarehouseStatus.ACTIVE;
}
