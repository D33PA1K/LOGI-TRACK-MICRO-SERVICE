package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.ShipmentDocumentDTO;
import com.cognizant.logitrack.enums.DocumentStatus;
import com.cognizant.logitrack.enums.DocumentType;
import com.cognizant.logitrack.service.ShipmentDocumentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shipment-documents")
public class ShipmentDocumentController {

    private final ShipmentDocumentService documentService;

    public ShipmentDocumentController(ShipmentDocumentService documentService) {
        this.documentService = documentService;
    }

    // Metadata-only create (JSON body). Kept for backward compatibility.
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ShipmentDocumentDTO> upload(@Valid @RequestBody ShipmentDocumentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.uploadDocument(dto));
    }

    // Real file upload (multipart). The selected file is stored on the server and
    // the entity keeps a relative path to it.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ShipmentDocumentDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("shipmentId") Integer shipmentId,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam(value = "submittedDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate submittedDate) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.uploadDocument(file, shipmentId, documentType, submittedDate));
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

