package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.ShipmentDocumentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "compliance-doc-service", contextId = "ShipmentDocumentClient", path = "/api/shipment-documents")
public interface ShipmentDocumentClient {
    @GetMapping("/shipment/{shipmentId}")
    List<ShipmentDocumentDTO> getByShipment(@PathVariable("shipmentId") Integer shipmentId);
}

