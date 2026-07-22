package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.FreightOrderDTO;
import com.cognizant.logitrack.enums.FreightOrderStatus;
import com.cognizant.logitrack.service.FreightOrderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/freight-orders")
@Slf4j
public class FreightOrderController {

    private final FreightOrderService freightOrderService;

    public FreightOrderController(FreightOrderService freightOrderService) {
        this.freightOrderService = freightOrderService;
    }

    @PostMapping
    public ResponseEntity<FreightOrderDTO> create(@Valid @RequestBody FreightOrderDTO dto) {
        log.info("Creating freight order for shipper {}", dto.getShipperId());
        return ResponseEntity.status(HttpStatus.CREATED).body(freightOrderService.createFreightOrder(dto));
    }

    @GetMapping
    public ResponseEntity<List<FreightOrderDTO>> getAll() {
        return ResponseEntity.ok(freightOrderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FreightOrderDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(freightOrderService.getById(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<FreightOrderDTO> cancel(@PathVariable Integer id) {
        return ResponseEntity.ok(freightOrderService.cancelOrder(id));
    }

    @GetMapping("/shipper/{shipperId}")
    public ResponseEntity<List<FreightOrderDTO>> getByShipper(@PathVariable Integer shipperId) {
        return ResponseEntity.ok(freightOrderService.getByShipper(shipperId));
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<FreightOrderDTO> updateStatus(
            @PathVariable Integer id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                freightOrderService.updateStatus(id, FreightOrderStatus.valueOf(status))
        );
    }
}

