package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.RouteDTO;
import com.cognizant.logitrack.enums.RouteMode;
import com.cognizant.logitrack.enums.RouteStatus;
import com.cognizant.logitrack.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping
    public ResponseEntity<RouteDTO> create(@Valid @RequestBody RouteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.addRoute(dto));
    }

    @GetMapping
    public ResponseEntity<List<RouteDTO>> getAll(@RequestParam(required = false) String mode) {
        if (mode != null) {
            return ResponseEntity.ok(routeService.getByMode(RouteMode.valueOf(mode)));
        }
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @GetMapping("/search")
    public ResponseEntity<RouteDTO> searchRoute(
            @RequestParam Integer origin,
            @RequestParam Integer destination,
            @RequestParam String status) {
        return routeService.findByOriginAndDestination(origin, destination, RouteStatus.valueOf(status))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(routeService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RouteDTO> updateStatus(@PathVariable Integer id, @RequestParam String status) {
        return ResponseEntity.ok(routeService.updateRouteStatus(id, RouteStatus.valueOf(status)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }
}

