package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.CarrierDTO;
import com.cognizant.logitrack.exception.FeignErrorSupport;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class CarrierClientFallbackFactory implements FallbackFactory<CarrierClient> {
    @Override
    public CarrierClient create(Throwable cause) {
        return new CarrierClient() {
            @Override
            public CarrierDTO getCarrierById(Integer id) {
                throw FeignErrorSupport.translate(cause,
                        "Carrier #" + id + " was not found.",
                        "Carrier service is unavailable — could not verify carrier #" + id
                                + " right now. Please try again shortly.");
            }
        };
    }
}
