package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.RateCardDTO;
import com.cognizant.logitrack.exception.FeignErrorSupport;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class RateCardClientFallbackFactory implements FallbackFactory<RateCardClient> {
    @Override
    public RateCardClient create(Throwable cause) {
        return new RateCardClient() {
            @Override
            public RateCardDTO getRateCardById(Integer id) {
                throw FeignErrorSupport.translate(cause,
                        "Rate card #" + id + " was not found.",
                        "Rate-card service is unavailable — could not verify rate card #" + id
                                + " right now. Please try again shortly.");
            }
        };
    }
}
