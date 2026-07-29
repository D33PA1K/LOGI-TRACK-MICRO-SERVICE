package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.PurchaseOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "supplier-po-service", contextId = "warehousePoClient", path = "/api/purchase-orders", fallbackFactory = PurchaseOrderClientFallbackFactory.class)
public interface PurchaseOrderClient {
    @GetMapping("/{id}")
    PurchaseOrderDTO getPurchaseOrderById(@PathVariable("id") Integer id);
}
