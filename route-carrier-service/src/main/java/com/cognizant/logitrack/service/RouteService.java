package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.RouteDTO;
import com.cognizant.logitrack.enums.RouteMode;
import com.cognizant.logitrack.enums.RouteStatus;
import java.util.List;
import java.util.Optional;

public interface RouteService {
    RouteDTO addRoute(RouteDTO dto);
    List<RouteDTO> getAllRoutes();
    List<RouteDTO> getByMode(RouteMode mode);
    RouteDTO getById(Integer id);
    RouteDTO updateRouteStatus(Integer id, RouteStatus status);
    void deleteRoute(Integer id);
    Optional<RouteDTO> findByOriginAndDestination(Integer origin, Integer destination, RouteStatus status);
}
