package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.ComplianceFlagDTO;
import com.cognizant.logitrack.service.ComplianceFlagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compliance-flags")
public class ComplianceFlagController {

    private final ComplianceFlagService flagService;

    public ComplianceFlagController(ComplianceFlagService flagService) {
        this.flagService = flagService;
    }

    @PostMapping
    public ResponseEntity<ComplianceFlagDTO> raise(@Valid @RequestBody ComplianceFlagDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flagService.raiseFlag(dto));
    }

    @GetMapping
    public ResponseEntity<List<ComplianceFlagDTO>> get(@RequestParam(required = false) Integer shipmentId,
                                                       @RequestParam(required = false) String status) {
        if (shipmentId != null) {
            return ResponseEntity.ok(flagService.getFlagsByShipment(shipmentId));
        }
        if (status != null && status.equalsIgnoreCase("OPEN")) {
            return ResponseEntity.ok(flagService.getOpenFlags());
        }
        return ResponseEntity.ok(flagService.getOpenFlags());
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ComplianceFlagDTO> resolve(@PathVariable Integer id) {
        return ResponseEntity.ok(flagService.resolveFlag(id));
    }
}

