package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.PurchaseOrderDTO;
import com.cognizant.logitrack.exception.FeignErrorSupport;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class PurchaseOrderClientFallbackFactory implements FallbackFactory<PurchaseOrderClient> {
    @Override
    public PurchaseOrderClient create(Throwable cause) {
        return new PurchaseOrderClient() {
            @Override
            public PurchaseOrderDTO getPurchaseOrderById(Integer id) {
                throw FeignErrorSupport.translate(cause,
                        "Purchase order #" + id + " was not found.",
                        "Supplier/PO service is unavailable — could not verify purchase order #" + id
                                + " right now. Please try again shortly.");
            }
        };
    }
}
