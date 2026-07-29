package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.NotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-alert-service", contextId = "warehouseNotificationClient", path = "/api/notifications", fallbackFactory = NotificationClientFallbackFactory.class)
public interface NotificationClient {
    @PostMapping
    NotificationDTO sendNotification(@RequestBody NotificationDTO dto);
}
