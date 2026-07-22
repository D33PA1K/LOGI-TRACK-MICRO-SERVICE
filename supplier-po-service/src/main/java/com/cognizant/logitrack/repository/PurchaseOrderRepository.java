package com.cognizant.logitrack.repository;

import com.cognizant.logitrack.entity.PurchaseOrder;
import com.cognizant.logitrack.enums.POStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer> {
    List<PurchaseOrder> findBySupplier_SupplierId(Integer supplierId);
    List<PurchaseOrder> findByStatus(POStatus status);
    List<PurchaseOrder> findByWarehouseId(Integer warehouseId);
}
