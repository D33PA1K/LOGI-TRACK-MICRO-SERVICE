package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.NotificationDTO;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Notifications are best-effort side effects — if the notification service is
 * down we must NOT fail the business transaction, so this fallback swallows the
 * error and returns null instead of throwing.
 */
@Component
@Slf4j
public class NotificationClientFallbackFactory implements FallbackFactory<NotificationClient> {
    @Override
    public NotificationClient create(Throwable cause) {
        return new NotificationClient() {
            @Override
            public NotificationDTO sendNotification(NotificationDTO dto) {
                log.warn("Notification service unavailable; notification dropped: {}",
                        cause.getMessage());
                return null;
            }
        };
    }
}
