package com.cognizant.logitrack.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * The computed metric set for a collection of shipments.
 *
 * Every report path (headline, per-scope breakdown, carrier scorecard) produces
 * this same shape from the same calculator, so two views of the same data can
 * never disagree about what the on-time rate is.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShipmentMetricsDTO {
    private long shipmentCount;
    private long deliveredCount;
    private long exceptionCount;

    /** % of delivered shipments that arrived on or before their estimated arrival. */
    private double onTimeRate;

    /** Mean (actualArrival - dispatchDate) in days across delivered shipments. */
    private double avgTransitDays;

    private BigDecimal totalFreightCost;

    /** % of all shipments currently in EXCEPTION or DELAYED. */
    private double exceptionRate;
}
