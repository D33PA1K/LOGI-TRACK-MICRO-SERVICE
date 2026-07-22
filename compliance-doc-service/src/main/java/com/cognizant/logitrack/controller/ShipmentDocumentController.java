package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.ShipmentDocumentDTO;
import com.cognizant.logitrack.enums.DocumentStatus;
import com.cognizant.logitrack.service.ShipmentDocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipment-documents")
public class ShipmentDocumentController {

    private final ShipmentDocumentService documentService;

    public ShipmentDocumentController(ShipmentDocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<ShipmentDocumentDTO> upload(@Valid @RequestBody ShipmentDocumentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.uploadDocument(dto));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentDocumentDTO>> getByShipment(@RequestParam Integer shipmentId) {
        return ResponseEntity.ok(documentService.getDocsByShipment(shipmentId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ShipmentDocumentDTO> updateStatus(@PathVariable Integer id, @RequestParam String status) {
        return ResponseEntity.ok(documentService.updateDocumentStatus(id, DocumentStatus.valueOf(status)));
    }
}

