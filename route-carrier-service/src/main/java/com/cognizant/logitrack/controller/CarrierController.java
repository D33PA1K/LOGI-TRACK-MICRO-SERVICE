package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.CarrierDTO;
import com.cognizant.logitrack.enums.CarrierStatus;
import com.cognizant.logitrack.enums.RouteMode;
import com.cognizant.logitrack.service.CarrierService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carriers")
public class CarrierController {

    private final CarrierService carrierService;

    public CarrierController(CarrierService carrierService) {
        this.carrierService = carrierService;
    }

    @PostMapping
    public ResponseEntity<CarrierDTO> create(@Valid @RequestBody CarrierDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carrierService.addCarrier(dto));
    }

    @GetMapping
    public ResponseEntity<List<CarrierDTO>> getAll(@RequestParam(required = false) String mode) {
        if (mode != null) {
            return ResponseEntity.ok(carrierService.getByMode(RouteMode.valueOf(mode)));
        }
        return ResponseEntity.ok(carrierService.getAllCarriers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarrierDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(carrierService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CarrierDTO> updateStatus(@PathVariable Integer id, @RequestParam String status) {
        return ResponseEntity.ok(carrierService.updateCarrierStatus(id, CarrierStatus.valueOf(status)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        carrierService.deleteCarrier(id);
        return ResponseEntity.noContent().build();
    }
}

