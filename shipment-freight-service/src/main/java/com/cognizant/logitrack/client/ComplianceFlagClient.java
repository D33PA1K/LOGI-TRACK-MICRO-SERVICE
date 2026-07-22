package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.ComplianceFlagDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "compliance-doc-service", contextId = "ComplianceFlagClient", path = "/api/compliance-flags")
public interface ComplianceFlagClient {
    @GetMapping("/shipment/{shipmentId}")
    List<ComplianceFlagDTO> getByShipment(@PathVariable("shipmentId") Integer shipmentId);
}

