package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.PickListDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "warehouse-inventory-service", contextId = "PickListClient", path = "/api/pick-lists", fallbackFactory = PickListClientFallbackFactory.class)
public interface PickListClient {
    @GetMapping("/freight-order/{freightOrderId}")
    List<PickListDTO> getByFreightOrder(@PathVariable("freightOrderId") Integer freightOrderId);
}

