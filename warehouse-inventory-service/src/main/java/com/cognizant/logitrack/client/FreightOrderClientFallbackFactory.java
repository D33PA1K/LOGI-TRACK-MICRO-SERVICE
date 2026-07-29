package com.cognizant.logitrack.client;

import com.cognizant.logitrack.exception.FeignErrorSupport;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class FreightOrderClientFallbackFactory implements FallbackFactory<FreightOrderClient> {
    @Override
    public FreightOrderClient create(Throwable cause) {
        return new FreightOrderClient() {
            @Override
            public Object getFreightOrderById(Integer id) {
                throw FeignErrorSupport.translate(cause,
                        "Freight order #" + id + " was not found.",
                        "Shipment/freight service is unavailable — could not verify freight order #" + id
                                + " right now. Please try again shortly.");
            }
        };
    }
}
