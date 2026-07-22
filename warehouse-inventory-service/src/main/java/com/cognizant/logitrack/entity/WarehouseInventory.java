package com.cognizant.logitrack.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "warehouse_inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer inventoryId;
    @Column(length = 50)
    private String sku;
    private String productName;
    private Integer warehouseId;
    private String binLocation;
    private Integer quantityOnHand;
    private Integer quantityReserved;
    @UpdateTimestamp
    private LocalDateTime lastUpdated;
}
