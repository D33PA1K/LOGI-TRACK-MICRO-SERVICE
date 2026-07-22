package com.cognizant.logitrack.dto;

import com.cognizant.logitrack.enums.RouteMode;
import com.cognizant.logitrack.enums.RouteStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteDTO {
    private Integer routeId;
    @NotNull
    private Integer originHubId;
    @NotNull
    private Integer destinationHubId;
    private Integer transitDays;
    @NotNull
    private RouteMode mode;
    private RouteStatus status;
}
