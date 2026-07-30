package com.cognizant.logitrack.client;

import com.cognizant.logitrack.dto.RouteDTO;
import com.cognizant.logitrack.exception.FeignErrorSupport;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class RouteClientFallbackFactory implements FallbackFactory<RouteClient> {
    @Override
    public RouteClient create(Throwable cause) {
        return new RouteClient() {
            @Override
            public RouteDTO getRouteById(Integer id) {
                throw FeignErrorSupport.translate(cause,
                        "Route #" + id + " was not found.",
                        "Route service is unavailable — could not load route #" + id
                                + " right now. Please try again shortly.");
            }

            @Override
            public RouteDTO searchRoute(Integer origin, Integer destination, String status) {
                throw FeignErrorSupport.translate(cause,
                        "No " + status + " route matches origin " + origin
                                + " to destination " + destination + ".",
                        "Route service is unavailable — could not search routes right now."
                                + " Please try again shortly.");
            }
        };
    }
}
