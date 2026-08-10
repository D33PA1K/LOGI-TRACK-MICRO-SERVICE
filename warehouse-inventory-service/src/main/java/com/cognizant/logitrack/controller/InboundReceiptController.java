package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.InboundReceiptDTO;
import com.cognizant.logitrack.enums.ReceiptStatus;
import com.cognizant.logitrack.service.InboundReceiptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inbound-receipts")
public class InboundReceiptController {

    private final InboundReceiptService inboundReceiptService;

    public InboundReceiptController(InboundReceiptService inboundReceiptService) {
        this.inboundReceiptService = inboundReceiptService;
    }

    @PostMapping
    public ResponseEntity<InboundReceiptDTO> create(@Valid @RequestBody InboundReceiptDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inboundReceiptService.createReceipt(dto));
    }

    @GetMapping
    public ResponseEntity<List<InboundReceiptDTO>> getByWarehouse(@RequestParam(required = false) Integer warehouseId) {
        return ResponseEntity.ok(warehouseId == null
                ? inboundReceiptService.getAllReceipts()
                : inboundReceiptService.getByWarehouse(warehouseId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<InboundReceiptDTO> updateStatus(@PathVariable Integer id, @RequestParam String status) {
        return ResponseEntity.ok(inboundReceiptService.updateStatus(id, ReceiptStatus.valueOf(status)));
    }
}

