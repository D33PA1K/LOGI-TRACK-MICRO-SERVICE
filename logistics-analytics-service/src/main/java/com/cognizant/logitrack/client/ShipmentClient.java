package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.ShipmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "shipment-freight-service", path = "/api/shipments", fallbackFactory = ShipmentClientFallbackFactory.class)
public interface ShipmentClient {
    @GetMapping("/{id}")
    ShipmentDTO getShipmentById(@PathVariable("id") Integer id);

    @GetMapping
    List<ShipmentDTO> getAllShipments();
}
