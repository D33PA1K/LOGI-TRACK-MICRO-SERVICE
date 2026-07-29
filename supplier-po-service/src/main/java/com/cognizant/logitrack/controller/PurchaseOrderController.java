package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.PurchaseOrderDTO;
import com.cognizant.logitrack.enums.POStatus;
import com.cognizant.logitrack.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderDTO> create(@Valid @RequestBody PurchaseOrderDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseOrderService.createPO(dto));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderDTO>> get(@RequestParam(required = false) Integer supplierId,
                                                      @RequestParam(required = false) Integer warehouseId) {
        if (supplierId != null && warehouseId != null) {
            return ResponseEntity.ok(purchaseOrderService.getPOsBySupplierAndWarehouse(supplierId, warehouseId));
        }
        if (supplierId != null) {
            return ResponseEntity.ok(purchaseOrderService.getPOsBySupplier(supplierId));
        }
        if (warehouseId != null) {
            return ResponseEntity.ok(purchaseOrderService.getPOsByWarehouse(warehouseId));
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(purchaseOrderService.getById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PurchaseOrderDTO> updateStatus(@PathVariable Integer id, @RequestParam String status) {
        return ResponseEntity.ok(purchaseOrderService.updatePOStatus(id, POStatus.valueOf(status)));
    }
}

