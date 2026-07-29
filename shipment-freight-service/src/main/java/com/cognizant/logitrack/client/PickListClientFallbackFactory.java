package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.PickListDTO;
import com.cognizant.logitrack.exception.FeignErrorSupport;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PickListClientFallbackFactory implements FallbackFactory<PickListClient> {
    @Override
    public PickListClient create(Throwable cause) {
        return new PickListClient() {
            @Override
            public List<PickListDTO> getByFreightOrder(Integer freightOrderId) {
                throw FeignErrorSupport.translate(cause,
                        "No pick lists found for freight order #" + freightOrderId + ".",
                        "Warehouse service is unavailable — could not verify pick lists for freight order #"
                                + freightOrderId + ". Please try again shortly.");
            }
        };
    }
}
