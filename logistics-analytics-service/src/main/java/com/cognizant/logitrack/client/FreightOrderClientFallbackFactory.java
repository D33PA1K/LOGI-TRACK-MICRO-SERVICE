package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.FreightOrderDTO;
import com.cognizant.logitrack.exception.FeignErrorSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class FreightOrderClientFallbackFactory implements FallbackFactory<FreightOrderClient> {

    @Override
    public FreightOrderClient create(Throwable cause) {
        return new FreightOrderClient() {
            @Override
            public List<FreightOrderDTO> getAllFreightOrders() {
                log.error("FreightOrderClient.getAllFreightOrders fallback: {}", cause.getMessage());
                throw FeignErrorSupport.translate(cause,
                        "Freight order data could not be retrieved for this report scope.",
                        "Shipment service is unavailable — cannot build a route- or hub-scoped report"
                                + " right now because freight order data is required."
                                + " Please try again shortly.");
            }
        };
    }
}
