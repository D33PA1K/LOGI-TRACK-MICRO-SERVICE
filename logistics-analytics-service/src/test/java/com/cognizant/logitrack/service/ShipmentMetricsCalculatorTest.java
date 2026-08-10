package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.ShipmentDTO;
import com.cognizant.logitrack.dto.ShipmentMetricsDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The KPI maths every report path depends on. These are the numbers an
 * interviewer or an operations analyst would challenge, so each rule is pinned.
 */
class ShipmentMetricsCalculatorTest {

    private static ShipmentDTO shipment(Integer id, String status, LocalDate dispatch,
                                        LocalDate estimated, LocalDate actual, String cost) {
        return ShipmentDTO.builder()
                .shipmentId(id)
                .carrierId(1)
                .status(status)
                .dispatchDate(dispatch)
                .estimatedArrival(estimated)
                .actualArrival(actual)
                .freightCost(cost == null ? null : new BigDecimal(cost))
                .build();
    }

    @Test
    @DisplayName("empty input reports zeros, never NaN")
    void emptyInputIsAllZeros() {
        ShipmentMetricsDTO metrics = ShipmentMetricsCalculator.compute(List.of());

        assertEquals(0, metrics.getShipmentCount());
        assertEquals(0.0, metrics.getOnTimeRate());
        assertEquals(0.0, metrics.getAvgTransitDays());
        assertEquals(0.0, metrics.getExceptionRate());
        assertEquals(0, new BigDecimal("0.00").compareTo(metrics.getTotalFreightCost()));
    }

    @Test
    @DisplayName("null input is treated as empty rather than throwing")
    void nullInputIsSafe() {
        assertEquals(0, ShipmentMetricsCalculator.compute(null).getShipmentCount());
    }

    @Test
    @DisplayName("arriving exactly on the estimated date counts as on time")
    void arrivingOnTheEstimatedDateIsOnTime() {
        List<ShipmentDTO> shipments = List.of(
                shipment(1, "DELIVERED", LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 4), LocalDate.of(2026, 8, 4), "100"));

        assertEquals(100.0, ShipmentMetricsCalculator.compute(shipments).getOnTimeRate());
    }

    @Test
    @DisplayName("on-time rate is over delivered shipments only, not all shipments")
    void onTimeRateIgnoresShipmentsStillInFlight() {
        List<ShipmentDTO> shipments = List.of(
                // 1 of 2 delivered shipments was late -> 50%, despite 4 shipments total.
                shipment(1, "DELIVERED", LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 4), LocalDate.of(2026, 8, 3), "100"),
                shipment(2, "DELIVERED", LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 4), LocalDate.of(2026, 8, 6), "100"),
                shipment(3, "INTRANSIT", LocalDate.of(2026, 8, 2),
                        LocalDate.of(2026, 8, 7), null, "100"),
                shipment(4, "PLANNED", LocalDate.of(2026, 8, 3), null, null, "100"));

        ShipmentMetricsDTO metrics = ShipmentMetricsCalculator.compute(shipments);

        assertEquals(4, metrics.getShipmentCount());
        assertEquals(2, metrics.getDeliveredCount());
        assertEquals(50.0, metrics.getOnTimeRate());
    }

    @Test
    @DisplayName("DELAYED and EXCEPTION both count toward the exception rate")
    void exceptionRateCountsDelayedAndException() {
        List<ShipmentDTO> shipments = List.of(
                shipment(1, "DELAYED", LocalDate.of(2026, 8, 1), null, null, "10"),
                shipment(2, "EXCEPTION", LocalDate.of(2026, 8, 1), null, null, "10"),
                shipment(3, "INTRANSIT", LocalDate.of(2026, 8, 1), null, null, "10"),
                shipment(4, "DELIVERED", LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 2), LocalDate.of(2026, 8, 2), "10"));

        ShipmentMetricsDTO metrics = ShipmentMetricsCalculator.compute(shipments);

        assertEquals(2, metrics.getExceptionCount());
        assertEquals(50.0, metrics.getExceptionRate());
    }

    @Test
    @DisplayName("freight cost sums every shipment and tolerates a null cost")
    void freightCostSumsAllShipmentsIgnoringNulls() {
        List<ShipmentDTO> shipments = List.of(
                shipment(1, "DELIVERED", LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 2), LocalDate.of(2026, 8, 2), "25000.50"),
                shipment(2, "INTRANSIT", LocalDate.of(2026, 8, 1), null, null, "1000.25"),
                shipment(3, "PLANNED", LocalDate.of(2026, 8, 1), null, null, null));

        assertEquals(0, new BigDecimal("26000.75")
                .compareTo(ShipmentMetricsCalculator.compute(shipments).getTotalFreightCost()));
    }

    @Test
    @DisplayName("average transit days is measured from dispatch to actual arrival")
    void avgTransitDaysUsesDispatchToActualArrival()  {
        List<ShipmentDTO> shipments = List.of(
                shipment(1, "DELIVERED", LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 5), LocalDate.of(2026, 8, 3), "10"),   // 2 days
                shipment(2, "DELIVERED", LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 5), LocalDate.of(2026, 8, 6), "10"));  // 5 days

        assertEquals(3.5, ShipmentMetricsCalculator.compute(shipments).getAvgTransitDays());
    }

    @Test
    @DisplayName("no date bounds returns the input untouched")
    void unboundedFilterReturnsEverything() {
        List<ShipmentDTO> shipments = List.of(
                shipment(1, "DELIVERED", LocalDate.of(2026, 8, 1), null, null, "10"),
                shipment(2, "DELIVERED", null, null, null, "10"));

        assertEquals(2, ShipmentMetricsCalculator
                .filterByDispatchDate(shipments, null, null).size());
    }

    @Test
    @DisplayName("date filter is inclusive on both bounds")
    void dateFilterIsInclusive() {
        List<ShipmentDTO> shipments = List.of(
                shipment(1, "DELIVERED", LocalDate.of(2026, 7, 31), null, null, "10"),
                shipment(2, "DELIVERED", LocalDate.of(2026, 8, 1), null, null, "10"),
                shipment(3, "DELIVERED", LocalDate.of(2026, 8, 15), null, null, "10"),
                shipment(4, "DELIVERED", LocalDate.of(2026, 8, 31), null, null, "10"),
                shipment(5, "DELIVERED", LocalDate.of(2026, 9, 1), null, null, "10"));

        List<ShipmentDTO> filtered = ShipmentMetricsCalculator.filterByDispatchDate(
                shipments, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertEquals(List.of(2, 3, 4),
                filtered.stream().map(ShipmentDTO::getShipmentId).toList());
    }

    @Test
    @DisplayName("a shipment with no dispatch date is excluded once a bound is set")
    void undatedShipmentIsExcludedWhenFiltering() {
        List<ShipmentDTO> shipments = List.of(
                shipment(1, "PLANNED", null, null, null, "10"));

        assertTrue(ShipmentMetricsCalculator
                .filterByDispatchDate(shipments, LocalDate.of(2026, 8, 1), null).isEmpty());
        assertEquals(1, ShipmentMetricsCalculator
                .filterByDispatchDate(shipments, null, null).size());
    }

    @Test
    @DisplayName("an open-ended lower bound still filters the upper side out")
    void onlyUpperBoundSupplied() {
        List<ShipmentDTO> shipments = List.of(
                shipment(1, "DELIVERED", LocalDate.of(2026, 7, 1), null, null, "10"),
                shipment(2, "DELIVERED", LocalDate.of(2026, 9, 1), null, null, "10"));

        List<ShipmentDTO> filtered = ShipmentMetricsCalculator.filterByDispatchDate(
                shipments, null, LocalDate.of(2026, 8, 1));

        assertEquals(1, filtered.size());
        assertEquals(1, filtered.get(0).getShipmentId());
    }
}
