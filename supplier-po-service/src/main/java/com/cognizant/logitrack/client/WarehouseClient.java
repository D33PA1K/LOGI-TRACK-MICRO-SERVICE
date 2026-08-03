package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.WarehouseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "warehouse-inventory-service", contextId = "supplierWarehouseClient", path = "/api/warehouses", fallbackFactory = WarehouseClientFallbackFactory.class)
public interface WarehouseClient {
    @GetMapping("/{id}")
    WarehouseDTO getWarehouseById(@PathVariable("id") Integer id);
}
