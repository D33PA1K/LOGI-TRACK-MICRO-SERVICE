package com.cognizant.logitrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Per-carrier performance record (spec 4.7: "carrier scorecards").
 *
 * Ranked by on-time rate so the worst performer is immediately visible, with
 * cost per shipment included because the cheapest carrier and the most reliable
 * carrier are rarely the same one.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarrierScorecardDTO {
    private Integer carrierId;
    private String carrierName;
    private String serviceLevel;
    private String status;

    private long shipmentCount;
    private long deliveredCount;
    private long exceptionCount;
    private double onTimeRate;
    private double avgTransitDays;
    private double exceptionRate;
    private BigDecimal totalFreightCost;
    private BigDecimal avgFreightCost;
}
