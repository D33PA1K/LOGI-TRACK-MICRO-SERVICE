package com.cognizant.logitrack.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {
    private Integer inventoryId;
    private String sku;
    private String productName;
    private Integer warehouseId;
    private String binLocation;
    private Integer quantityOnHand;
    private Integer quantityReserved;
    private LocalDateTime lastUpdated;
}
