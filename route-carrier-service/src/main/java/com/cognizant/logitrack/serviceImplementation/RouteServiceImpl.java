package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.RouteService;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.RouteDTO;
import com.cognizant.logitrack.entity.Route;
import com.cognizant.logitrack.enums.RouteMode;
import com.cognizant.logitrack.enums.RouteStatus;
import com.cognizant.logitrack.repository.RouteRepository;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;

    public RouteServiceImpl(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Override
    public RouteDTO addRoute(RouteDTO dto) {

        if (dto.getOriginHubId().equals(dto.getDestinationHubId())) {
            throw new BadRequestException("Origin hub and destination hub cannot be same");
        }

        if (dto.getTransitDays() != null && dto.getTransitDays() <= 0) {
            throw new BadRequestException("Transit days must be greater than zero");
        }

        boolean routeExists = routeRepository.existsByOriginHubIdAndDestinationHubIdAndModeAndStatus(
                dto.getOriginHubId(),
                dto.getDestinationHubId(),
                dto.getMode(),
                RouteStatus.ACTIVE
        );

        if (routeExists) {
            throw new BadRequestException(
                    "Active route already exists for originHubId: "
                            + dto.getOriginHubId()
                            + ", destinationHubId: "
                            + dto.getDestinationHubId()
                            + ", mode: "
                            + dto.getMode()
            );
        }

        Route route = Route.builder()
                .originHubId(dto.getOriginHubId())
                .destinationHubId(dto.getDestinationHubId())
                .transitDays(dto.getTransitDays())
                .mode(dto.getMode())
                .status(RouteStatus.ACTIVE)
                .build();

        Route saved = routeRepository.save(route);

        log.info("Route added: id={}", saved.getRouteId());

        return toDTO(saved);
    }

    @Override
    public List<RouteDTO> getAllRoutes() {
        return routeRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RouteDTO> getByMode(RouteMode mode) {
        return routeRepository.findByMode(mode)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RouteDTO getById(Integer id) {
        return toDTO(findEntity(id));
    }

    @Override
    public RouteDTO updateRouteStatus(Integer id, RouteStatus status) {
        Route route = findEntity(id);
        route.setStatus(status);
        return toDTO(routeRepository.save(route));
    }

    @Override
    public void deleteRoute(Integer id) {
        Route route = findEntity(id);
        route.setStatus(RouteStatus.INACTIVE);
        routeRepository.save(route);
        log.info("Route soft-deleted: id={}", id);
    }

    private Route findEntity(Integer id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Route not found with id: " + id
                ));
    }

    private RouteDTO toDTO(Route route) {
        return RouteDTO.builder()
                .routeId(route.getRouteId())
                .originHubId(route.getOriginHubId())
                .destinationHubId(route.getDestinationHubId())
                .transitDays(route.getTransitDays())
                .mode(route.getMode())
                .status(route.getStatus())
                .build();
    }

    @Override
    public Optional<RouteDTO> findByOriginAndDestination(Integer origin, Integer destination, RouteStatus status) {
        return routeRepository.findFirstByOriginHubIdAndDestinationHubIdAndStatus(origin, destination, status)
                .map(this::toDTO);
    }
}