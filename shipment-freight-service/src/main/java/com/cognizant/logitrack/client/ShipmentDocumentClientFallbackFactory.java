package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.ShipmentDocumentDTO;
import com.cognizant.logitrack.exception.FeignErrorSupport;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShipmentDocumentClientFallbackFactory implements FallbackFactory<ShipmentDocumentClient> {
    @Override
    public ShipmentDocumentClient create(Throwable cause) {
        return new ShipmentDocumentClient() {
            @Override
            public List<ShipmentDocumentDTO> getByShipment(Integer shipmentId) {
                throw FeignErrorSupport.translate(cause,
                        "No documents found for shipment #" + shipmentId + ".",
                        "Documents service is unavailable — could not verify documents for shipment #"
                                + shipmentId + ". Please try again shortly.");
            }
        };
    }
}
