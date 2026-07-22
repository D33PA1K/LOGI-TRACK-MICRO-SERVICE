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

    @GetMapping
    public ResponseEntity<List<InventoryDTO>> getByWarehouse(@RequestParam Integer warehouseId) {
        return ResponseEntity.ok(inventoryService.getInventoryByWarehouse(warehouseId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(inventoryService.getById(id));
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<InventoryDTO> updateQuantity(@PathVariable Integer id, @RequestParam Integer quantity) {
        return ResponseEntity.ok(inventoryService.updateQuantity(id, quantity));
    }

    @PostMapping("/{id}/consume")
    public ResponseEntity<InventoryDTO> consumeStock(@PathVariable Integer id, @RequestParam Integer quantity) {
        return ResponseEntity.ok(inventoryService.consumeStock(id, quantity));
    }
}

