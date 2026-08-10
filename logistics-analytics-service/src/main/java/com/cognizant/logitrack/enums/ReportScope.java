package com.cognizant.logitrack.enums;

/**
 * The dimension a report is broken down by, per section 4.7 of the spec
 * ("Scope (Carrier/Route/Period/Hub)").
 *
 * GLOBAL produces headline metrics only; every other scope additionally
 * produces a per-group breakdown alongside those same headline metrics.
 */
public enum ReportScope {
    GLOBAL,
    CARRIER,
    ROUTE,
    HUB,
    PERIOD;

    /** Lenient parse: blank/unknown input falls back to GLOBAL rather than failing a report. */
    public static ReportScope from(String raw) {
        if (raw == null || raw.isBlank()) {
            return GLOBAL;
        }

        try {
            return ReportScope.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return GLOBAL;
        }
    }
}
