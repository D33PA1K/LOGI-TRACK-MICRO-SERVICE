package com.cognizant.logitrack.entity;

import com.cognizant.logitrack.enums.RouteMode;
import com.cognizant.logitrack.enums.RouteStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer routeId;
    private Integer originHubId;
    private Integer destinationHubId;
    private Integer transitDays;
    @Enumerated(EnumType.STRING)
    private RouteMode mode;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RouteStatus status = RouteStatus.ACTIVE;
}
