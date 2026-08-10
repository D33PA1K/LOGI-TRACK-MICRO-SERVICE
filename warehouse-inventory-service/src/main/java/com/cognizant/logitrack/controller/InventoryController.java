package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.InventoryDTO;
import com.cognizant.logitrack.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * warehouseId is optional: omitting it returns every warehouse's stock, which
     * is what the UI's "Any warehouse" option means. It used to be mandatory, so
     * that option could never return anything.
     */
    @GetMapping
    public ResponseEntity<List<InventoryDTO>> getByWarehouse(
            @RequestParam(required = false) Integer warehouseId) {
        return ResponseEntity.ok(warehouseId == null
                ? inventoryService.getAllInventory()
                : inventoryService.getInventoryByWarehouse(warehouseId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(inventoryService.getById(id));
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<InventoryDTO> updateQuantity(@PathVariable Integer id, @RequestParam Integer quantity) {
        return ResponseEntity.ok(inventoryService.updateQuantity(id, quantity));
    }

    // Reserve/release were implemented in the service but had no endpoint, so
    // bin-level reservation was unreachable. Both are exposed here and are also
    // driven automatically by the pick-list lifecycle.
    @PostMapping("/{id}/reserve")
    public ResponseEntity<InventoryDTO> reserveStock(@PathVariable Integer id, @RequestParam Integer quantity) {
        return ResponseEntity.ok(inventoryService.reserveStock(id, quantity));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<InventoryDTO> releaseStock(@PathVariable Integer id, @RequestParam Integer quantity) {
        return ResponseEntity.ok(inventoryService.releaseStock(id, quantity));
    }

    @PostMapping("/{id}/consume")
    public ResponseEntity<InventoryDTO> consumeStock(@PathVariable Integer id, @RequestParam Integer quantity) {
        return ResponseEntity.ok(inventoryService.consumeStock(id, quantity));
    }
}

