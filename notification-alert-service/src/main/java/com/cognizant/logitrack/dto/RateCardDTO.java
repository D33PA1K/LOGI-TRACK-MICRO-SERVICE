package com.cognizant.logitrack.dto;

import com.cognizant.logitrack.enums.RateCardStatus;
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
public class RateCardDTO {
    private Integer rateCardId;
    @NotNull
    private Integer carrierId;
    @NotNull
    private Integer routeId;
    @NotNull
    private BigDecimal baseRate;
    @NotNull
    private String weightSlab;
    @NotNull
    private LocalDate effectiveDate;
    @NotNull
    private LocalDate expiryDate;
    private RateCardStatus status;
}
