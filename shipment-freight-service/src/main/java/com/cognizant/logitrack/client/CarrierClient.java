package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.CarrierDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "route-carrier-service", contextId = "CarrierClient", path = "/api/carriers")
public interface CarrierClient {
    @GetMapping("/{id}")
    CarrierDTO getCarrierById(@PathVariable("id") Integer id);
}

