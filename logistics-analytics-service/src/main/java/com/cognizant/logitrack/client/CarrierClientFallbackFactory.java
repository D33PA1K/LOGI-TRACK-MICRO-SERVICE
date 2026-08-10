package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.CarrierDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carrier names are presentation detail, not report data: if route-carrier-service
 * is unreachable this degrades to an empty list so the scorecard still renders
 * (labelled "Carrier #3" instead of by name) rather than failing the whole report.
 * This is the one client here that deliberately does NOT propagate the failure.
 */
@Component
@Slf4j
public class CarrierClientFallbackFactory implements FallbackFactory<CarrierClient> {

    @Override
    public CarrierClient create(Throwable cause) {
        return new CarrierClient() {
            @Override
            public List<CarrierDTO> getAllCarriers() {
                log.warn("CarrierClient.getAllCarriers fallback — carrier names unavailable: {}",
                        cause.getMessage());
                return List.of();
            }
        };
    }
}
