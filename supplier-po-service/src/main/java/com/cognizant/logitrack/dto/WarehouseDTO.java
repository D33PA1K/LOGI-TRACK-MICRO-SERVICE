package com.cognizant.logitrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Local view of a warehouse owned by warehouse-inventory-service. Only the
// fields needed to confirm a warehouse exists are mapped.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseDTO {
    private Integer warehouseId;
    private String warehouseName;
    private String status;
}
