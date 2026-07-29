package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.RouteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "route-carrier-service", contextId = "RouteClient", path = "/api/routes", fallbackFactory = RouteClientFallbackFactory.class)
public interface RouteClient {
    @GetMapping("/{id}")
    RouteDTO getRouteById(@PathVariable("id") Integer id);

    @GetMapping("/search")
    RouteDTO searchRoute(
            @RequestParam("origin") Integer origin,
            @RequestParam("destination") Integer destination,
            @RequestParam("status") String status);
}

