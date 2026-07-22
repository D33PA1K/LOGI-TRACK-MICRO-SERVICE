package com.cognizant.logitrack.repository;

import com.cognizant.logitrack.entity.Route;
import com.cognizant.logitrack.enums.RouteMode;
import com.cognizant.logitrack.enums.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Integer> {

    List<Route> findByStatus(RouteStatus status);

    List<Route> findByMode(RouteMode mode);

    Optional<Route> findFirstByOriginHubIdAndDestinationHubIdAndStatus(
            Integer originHubId,
            Integer destinationHubId,
            RouteStatus status
    );

    boolean existsByOriginHubIdAndDestinationHubIdAndModeAndStatus(
            Integer originHubId,
            Integer destinationHubId,
            RouteMode mode,
            RouteStatus status
    );
}