package com.cognizant.logitrack.dto;

import com.cognizant.logitrack.enums.CarrierServiceLevel;
import com.cognizant.logitrack.enums.CarrierStatus;
import com.cognizant.logitrack.enums.RouteMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarrierDTO {
    private Integer carrierId;
    @NotBlank
    private String name;
    @NotNull
    private RouteMode mode;
    @NotNull
    private CarrierServiceLevel serviceLevel;
    private String contactDetails;
    private CarrierStatus status;
}
