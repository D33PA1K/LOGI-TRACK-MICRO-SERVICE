package com.cognizant.logitrack.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Report parameters.
 *
 * Dates are LocalDate, not LocalDateTime: the UI collects them from
 * &lt;input type="date"&gt; and shipments are dated by calendar day, so
 * LocalDateTime forced the client to invent a time component (and previously
 * failed deserialization when it sent a plain "2026-08-01").
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {

    /** One of GLOBAL, CARRIER, ROUTE, HUB, PERIOD. Defaults to GLOBAL. */
    private String scope;

    /** Inclusive lower bound on shipment dispatch date. Null means unbounded. */
    private LocalDate fromDate;

    /** Inclusive upper bound on shipment dispatch date. Null means unbounded. */
    private LocalDate toDate;
}
