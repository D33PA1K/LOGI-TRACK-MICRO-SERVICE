package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.RateCardDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "route-carrier-service", contextId = "RateCardClient", path = "/api/rate-cards", fallbackFactory = RateCardClientFallbackFactory.class)
public interface RateCardClient {
    @GetMapping("/{id}")
    RateCardDTO getRateCardById(@PathVariable("id") Integer id);
}

