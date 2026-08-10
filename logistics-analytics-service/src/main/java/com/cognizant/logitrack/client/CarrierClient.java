package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.CarrierDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

/**
 * Carrier master data, used to label carrier-scoped reports and scorecards with
 * a real carrier name instead of a bare id.
 */
@FeignClient(
        name = "route-carrier-service",
        contextId = "CarrierClient",
        path = "/api/carriers",
        fallbackFactory = CarrierClientFallbackFactory.class)
public interface CarrierClient {

    @GetMapping
    List<CarrierDTO> getAllCarriers();
}
