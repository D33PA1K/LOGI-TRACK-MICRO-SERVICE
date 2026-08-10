package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.FreightOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

/**
 * Freight orders carry the route and origin/destination hubs that shipments do
 * not, so ROUTE- and HUB-scoped reports need them. Only called for those scopes.
 */
@FeignClient(
        name = "shipment-freight-service",
        contextId = "FreightOrderClient",
        path = "/api/freight-orders",
        fallbackFactory = FreightOrderClientFallbackFactory.class)
public interface FreightOrderClient {

    @GetMapping
    List<FreightOrderDTO> getAllFreightOrders();
}
