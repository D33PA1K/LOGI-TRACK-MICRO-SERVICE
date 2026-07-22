package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.PickListDTO;
import com.cognizant.logitrack.enums.PickListStatus;
import com.cognizant.logitrack.service.PickListService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pick-lists")
public class PickListController {

    private final PickListService pickListService;

    public PickListController(PickListService pickListService) {
        this.pickListService = pickListService;
    }

    @PostMapping
    public ResponseEntity<PickListDTO> create(@Valid @RequestBody PickListDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pickListService.createPickList(dto));
    }

    @GetMapping
    public ResponseEntity<List<PickListDTO>> getByWarehouse(@RequestParam Integer warehouseId) {
        return ResponseEntity.ok(pickListService.getByWarehouse(warehouseId));
    }

    @GetMapping("/assigned/{userId}")
    public ResponseEntity<List<PickListDTO>> getByAssignedUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(pickListService.getByAssignedUser(userId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PickListDTO> updateStatus(@PathVariable Integer id, @RequestParam String status) {
        return ResponseEntity.ok(pickListService.updatePickListStatus(id, PickListStatus.valueOf(status)));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<PickListDTO> assign(@PathVariable Integer id, @RequestParam Integer assignedTo) {
        return ResponseEntity.ok(pickListService.assignPickList(id, assignedTo));
    }
}

