package com.cognizant.logitrack.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "identity-access-service", contextId = "shipmentIdentityClient", path = "/api/users", fallbackFactory = IdentityClientFallbackFactory.class)
public interface IdentityClient {
    @GetMapping("/{id}")
    Object getUserById(@PathVariable("id") Integer id);
}
