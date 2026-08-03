package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.WarehouseDTO;
import com.cognizant.logitrack.exception.FeignErrorSupport;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class WarehouseClientFallbackFactory implements FallbackFactory<WarehouseClient> {
    @Override
    public WarehouseClient create(Throwable cause) {
        return new WarehouseClient() {
            @Override
            public WarehouseDTO getWarehouseById(Integer id) {
                throw FeignErrorSupport.translate(cause,
                        "Warehouse #" + id + " does not exist.",
                        "Warehouse service is unavailable — could not verify warehouse #" + id
                                + " right now. Please try again shortly.");
            }
        };
    }
}
