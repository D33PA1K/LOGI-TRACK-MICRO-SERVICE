package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.ShipmentDTO;
import com.cognizant.logitrack.dto.ShipmentMetricsDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The single place shipment KPIs are computed.
 *
 * Money and rates use BigDecimal with HALF_UP rounding rather than raw double
 * arithmetic, and every average is guarded against an empty input so an empty
 * period reports 0.0 instead of NaN.
 */
public final class ShipmentMetricsCalculator {

    private static final String STATUS_DELIVERED = "DELIVERED";
    private static final String STATUS_EXCEPTION = "EXCEPTION";
    private static final String STATUS_DELAYED = "DELAYED";

    private ShipmentMetricsCalculator() {
    }

    public static ShipmentMetricsDTO compute(List<ShipmentDTO> shipments) {
        if (shipments == null || shipments.isEmpty()) {
            return ShipmentMetricsDTO.builder()
                    .shipmentCount(0)
                    .deliveredCount(0)
                    .exceptionCount(0)
                    .onTimeRate(0.0)
                    .avgTransitDays(0.0)
                    .totalFreightCost(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .exceptionRate(0.0)
                    .build();
        }

        long shipmentCount = shipments.size();

        long deliveredCount = shipments.stream()
                .filter(s -> STATUS_DELIVERED.equals(s.getStatus()))
                .count();

        long exceptionCount = shipments.stream()
                .filter(ShipmentMetricsCalculator::isException)
                .count();

        // On-time is only meaningful for shipments that have both an actual and
        // an estimated arrival; anything still in flight is excluded rather than
        // silently counted as late.
        List<ShipmentDTO> delivered = shipments.stream()
                .filter(s -> STATUS_DELIVERED.equals(s.getStatus())
                        && s.getActualArrival() != null
                        && s.getEstimatedArrival() != null)
                .collect(Collectors.toList());

        long onTimeCount = delivered.stream()
                .filter(s -> !s.getActualArrival().isAfter(s.getEstimatedArrival()))
                .count();

        double onTimeRate = percentage(onTimeCount, delivered.size());

        double avgTransitDays = shipments.stream()
                .filter(s -> STATUS_DELIVERED.equals(s.getStatus())
                        && s.getActualArrival() != null
                        && s.getDispatchDate() != null)
                .mapToLong(s -> ChronoUnit.DAYS.between(s.getDispatchDate(), s.getActualArrival()))
                .average()
                .orElse(0.0);

        BigDecimal totalFreightCost = shipments.stream()
                .map(ShipmentDTO::getFreightCost)
                .filter(cost -> cost != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return ShipmentMetricsDTO.builder()
                .shipmentCount(shipmentCount)
                .deliveredCount(deliveredCount)
                .exceptionCount(exceptionCount)
                .onTimeRate(onTimeRate)
                .avgTransitDays(round1(avgTransitDays))
                .totalFreightCost(totalFreightCost)
                .exceptionRate(percentage(exceptionCount, shipmentCount))
                .build();
    }

    public static boolean isException(ShipmentDTO shipment) {
        return STATUS_EXCEPTION.equals(shipment.getStatus())
                || STATUS_DELAYED.equals(shipment.getStatus());
    }

    /**
     * Restricts shipments to those dispatched inside the (inclusive) window.
     * A null bound means "unbounded on that side"; when any bound is supplied,
     * a shipment with no dispatch date cannot be placed in time and is excluded.
     */
    public static List<ShipmentDTO> filterByDispatchDate(List<ShipmentDTO> shipments,
                                                         LocalDate from,
                                                         LocalDate to) {
        if (shipments == null) {
            return List.of();
        }

        if (from == null && to == null) {
            return shipments;
        }

        return shipments.stream()
                .filter(s -> {
                    LocalDate dispatchDate = s.getDispatchDate();

                    if (dispatchDate == null) {
                        return false;
                    }

                    if (from != null && dispatchDate.isBefore(from)) {
                        return false;
                    }

                    return to == null || !dispatchDate.isAfter(to);
                })
                .collect(Collectors.toList());
    }

    private static double percentage(long part, long whole) {
        if (whole <= 0) {
            return 0.0;
        }

        return BigDecimal.valueOf((double) part / whole * 100)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static double round1(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
