package com.cognizant.logitrack.repository;

import com.cognizant.logitrack.entity.WarehouseInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseInventoryRepository extends JpaRepository<WarehouseInventory, Integer> {
    List<WarehouseInventory> findByWarehouseId(Integer warehouseId);
    List<WarehouseInventory> findBySku(String sku);
    List<WarehouseInventory> findByWarehouseIdAndBinLocation(Integer warehouseId, String binLocation);
}
