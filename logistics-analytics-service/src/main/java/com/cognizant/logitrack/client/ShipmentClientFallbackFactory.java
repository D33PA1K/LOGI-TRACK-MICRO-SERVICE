package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.ShipmentDTO;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ShipmentClientFallbackFactory implements FallbackFactory<ShipmentClient> {
    @Override
    public ShipmentClient create(Throwable cause) {
        return new ShipmentClient() {
            @Override
            public ShipmentDTO getShipmentById(Integer id) {
                log.error("Fallback triggered for getShipmentById: {}", cause.getMessage());
                return null;
            }

            @Override
            public List<ShipmentDTO> getAllShipments() {
                log.error("Fallback triggered for getAllShipments: {}", cause.getMessage());
                return Collections.emptyList();
            }
        };
    }
}
