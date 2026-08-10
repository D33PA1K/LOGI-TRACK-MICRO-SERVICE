package com.cognizant.logitrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One row of a scoped report: the group, its display label, and its metrics. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScopeBreakdownDTO {

    /** Stable machine key for the group, e.g. "3" for carrier #3 or "2026-08". */
    private String key;

    /** Human label, e.g. "BlueDart Express (#3)" or "Hub 1 -> Hub 2". */
    private String label;

    private ShipmentMetricsDTO metrics;
}
