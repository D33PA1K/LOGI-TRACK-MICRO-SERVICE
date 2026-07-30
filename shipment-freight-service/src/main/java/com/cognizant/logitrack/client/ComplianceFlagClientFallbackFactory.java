package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.ComplianceFlagDTO;
import com.cognizant.logitrack.exception.FeignErrorSupport;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ComplianceFlagClientFallbackFactory implements FallbackFactory<ComplianceFlagClient> {
    @Override
    public ComplianceFlagClient create(Throwable cause) {
        return new ComplianceFlagClient() {
            @Override
            public List<ComplianceFlagDTO> getByShipment(Integer shipmentId) {
                throw FeignErrorSupport.translate(cause,
                        "No compliance flags found for shipment #" + shipmentId + ".",
                        "Compliance service is unavailable — could not verify compliance flags for shipment #"
                                + shipmentId + ". Please try again shortly.");
            }
        };
    }
}
