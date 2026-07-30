package com.cognizant.logitrack.client;

import com.cognizant.logitrack.exception.FeignErrorSupport;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class IdentityClientFallbackFactory implements FallbackFactory<IdentityClient> {
    @Override
    public IdentityClient create(Throwable cause) {
        return new IdentityClient() {
            @Override
            public Object getUserById(Integer id) {
                throw FeignErrorSupport.translate(cause,
                        "User #" + id + " was not found.",
                        "Identity service is unavailable — could not verify user #" + id
                                + " right now. Please try again shortly.");
            }
        };
    }
}
