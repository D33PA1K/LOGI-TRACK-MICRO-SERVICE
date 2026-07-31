package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.UserDTO;
import com.cognizant.logitrack.exception.FeignErrorSupport;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class IdentityClientFallbackFactory implements FallbackFactory<IdentityClient> {
    @Override
    public IdentityClient create(Throwable cause) {
        return new IdentityClient() {
            @Override
            public UserDTO getUserById(Integer id) {
                throw FeignErrorSupport.translate(cause,
                        "No user found with id " + id + ". Give a valid shipper ID.",
                        "Identity service is unavailable — could not verify shipper #" + id
                                + " right now. Please try again shortly.");
            }
        };
    }
}
