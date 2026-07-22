package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.PurchaseOrderDTO;
import com.cognizant.logitrack.enums.POStatus;
import java.util.List;

public interface PurchaseOrderService {
    PurchaseOrderDTO createPO(PurchaseOrderDTO dto);
    List<PurchaseOrderDTO> getPOsBySupplier(Integer supplierId);
    List<PurchaseOrderDTO> getPOsByWarehouse(Integer warehouseId);
    PurchaseOrderDTO updatePOStatus(Integer id, POStatus status);
    PurchaseOrderDTO getById(Integer id);
}
