package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.WarehouseDTO;
import com.cognizant.logitrack.service.WarehouseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public ResponseEntity<List<WarehouseDTO>> getAll() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(warehouseService.getById(id));
    }
}
