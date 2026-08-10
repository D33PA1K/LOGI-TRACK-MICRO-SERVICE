package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.InventoryDTO;
import java.util.List;

public interface InventoryService {
    /** Every inventory row across all warehouses — backs the "Any warehouse" view. */
    List<InventoryDTO> getAllInventory();

    List<InventoryDTO> getInventoryByWarehouse(Integer warehouseId);
    InventoryDTO getById(Integer id);
    InventoryDTO updateQuantity(Integer id, Integer quantity);
    InventoryDTO reserveStock(Integer id, Integer quantity);
    InventoryDTO releaseStock(Integer id, Integer quantity);
    InventoryDTO consumeStock(Integer id, Integer quantity);
}
