package com.cognizant.logitrack.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "shipment-freight-service", contextId = "warehouseFreightOrderClient", path = "/api/freight-orders")
public interface FreightOrderClient {
    @GetMapping("/{id}")
    Object getFreightOrderById(@PathVariable("id") Integer id);
}
