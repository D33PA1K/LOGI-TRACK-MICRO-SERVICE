package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.RateCardDTO;
import com.cognizant.logitrack.enums.RateCardStatus;
import com.cognizant.logitrack.service.RateCardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rate-cards")
public class RateCardController {

    private final RateCardService rateCardService;

    public RateCardController(RateCardService rateCardService) {
        this.rateCardService = rateCardService;
    }

    @PostMapping
    public ResponseEntity<RateCardDTO> create(@Valid @RequestBody RateCardDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rateCardService.addRateCard(dto));
    }

    @GetMapping
    public ResponseEntity<List<RateCardDTO>> get(@RequestParam(required = false) Integer carrierId,
                                                 @RequestParam(required = false) Integer routeId) {
        // When both filters are supplied the result must match BOTH the carrier
        // and the route, not just the first one checked.
        if (carrierId != null && routeId != null) {
            return ResponseEntity.ok(rateCardService.getRateCardsByCarrierAndRoute(carrierId, routeId));
        }
        if (carrierId != null) {
            return ResponseEntity.ok(rateCardService.getRateCardsByCarrier(carrierId));
        }
        if (routeId != null) {
            return ResponseEntity.ok(rateCardService.getRateCardsByRoute(routeId));
        }
        // No filters supplied: return every rate card so the UI can show the full
        // list by default (and reset back to it after a filtered search).
        return ResponseEntity.ok(rateCardService.getAllRateCards());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RateCardDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(rateCardService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RateCardDTO> updateStatus(@PathVariable Integer id, @RequestParam String status) {
        return ResponseEntity.ok(rateCardService.updateStatus(id, RateCardStatus.valueOf(status)));
    }
}

