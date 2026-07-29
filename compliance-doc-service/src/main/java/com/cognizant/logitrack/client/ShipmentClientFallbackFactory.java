package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.ShipmentDTO;
import com.cognizant.logitrack.exception.FeignErrorSupport;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Component
@Slf4j
public class ShipmentClientFallbackFactory implements FallbackFactory<ShipmentClient> {
    @Override
    public ShipmentClient create(Throwable cause) {
        return new ShipmentClient() {
            @Override
            public ShipmentDTO getShipmentById(Integer id) {
                log.error("ShipmentClient.getShipmentById fallback: {}", cause.getMessage());
                throw FeignErrorSupport.translate(cause,
                        "Shipment #" + id + " was not found.",
                        "Shipment service is unavailable — could not verify shipment #" + id
                                + " right now. Please try again shortly.");
            }

            @Override
            public List<ShipmentDTO> getAllShipments() {
                log.error("ShipmentClient.getAllShipments fallback: {}", cause.getMessage());
                throw FeignErrorSupport.translate(cause,
                        "Shipments could not be retrieved.",
                        "Shipment service is unavailable — could not load shipments right now."
                                + " Please try again shortly.");
            }
        };
    }
}
