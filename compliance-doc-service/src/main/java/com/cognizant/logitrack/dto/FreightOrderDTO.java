package com.cognizant.logitrack.dto;

import com.cognizant.logitrack.enums.FreightOrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreightOrderDTO {

    private Integer freightOrderId;

    @NotNull
    private Integer shipperId;

    @NotNull
    private Integer originLocationId;

    @NotNull
    private Integer destinationLocationId;

    private Integer routeId;

    @NotBlank
    private String cargoDescription;

    @NotNull
    private BigDecimal weight;

    @NotNull
    private BigDecimal volume;

    @NotNull
    private LocalDate requiredDeliveryDate;

    private FreightOrderStatus status;
}