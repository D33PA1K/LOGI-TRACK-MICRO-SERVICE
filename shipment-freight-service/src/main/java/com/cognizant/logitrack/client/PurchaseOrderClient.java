package com.cognizant.logitrack.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.cognizant.logitrack.dto.PurchaseOrderDTO;

@FeignClient(name = "supplier-po-service", contextId = "shipmentPoClient", path = "/api/purchase-orders")
public interface PurchaseOrderClient {
    @GetMapping("/{id}")
    PurchaseOrderDTO getPurchaseOrderById(@PathVariable("id") Integer id);
}
