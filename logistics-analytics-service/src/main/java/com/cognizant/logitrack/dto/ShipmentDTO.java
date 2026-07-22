package com.cognizant.logitrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentDTO {
    private Integer shipmentId;
    private Integer freightOrderId;
    private Integer carrierId;
    private String trackingNumber;
    private String status;
    private LocalDate dispatchDate;
    private LocalDate estimatedArrival;
    private LocalDate actualArrival;
    private BigDecimal freightCost;
}
