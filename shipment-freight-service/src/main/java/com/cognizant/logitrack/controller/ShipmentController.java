package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.DeliveryEventDTO;
import com.cognizant.logitrack.dto.ShipmentDTO;
import com.cognizant.logitrack.enums.ShipmentStatus;
import com.cognizant.logitrack.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@Slf4j
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ResponseEntity<ShipmentDTO> create(@Valid @RequestBody ShipmentDTO dto) {
        log.info("Creating shipment for freight order {}", dto.getFreightOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(shipmentService.createShipment(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(shipmentService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentDTO>> getAllShipments() {
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ShipmentDTO> updateStatus(@PathVariable Integer id, @RequestParam String status) {
        return ResponseEntity.ok(shipmentService.updateShipmentStatus(id, ShipmentStatus.valueOf(status)));
    }

    @PostMapping("/{id}/dispatch")
    public ResponseEntity<ShipmentDTO> dispatch(@PathVariable Integer id) {
        log.info("Dispatching shipment {}", id);
        return ResponseEntity.ok(shipmentService.dispatchShipment(id));
    }

    @PostMapping("/{shipmentId}/events")
    public ResponseEntity<DeliveryEventDTO> addEvent(@PathVariable Integer shipmentId,
                                                     @Valid @RequestBody DeliveryEventDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shipmentService.addDeliveryEvent(shipmentId, dto));
    }

    @GetMapping("/{shipmentId}/events")
    public ResponseEntity<List<DeliveryEventDTO>> getEvents(@PathVariable Integer shipmentId) {
        return ResponseEntity.ok(shipmentService.getEventsByShipment(shipmentId));
    }
}

