package com.cognizant.logitrack.service;

import com.cognizant.logitrack.enums.ShipmentStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The legal shipment status transitions.
 *
 * Two things this exists to prevent:
 *
 * 1. Nonsense history — without it, PATCH /status accepts any value, so a
 *    DELIVERED shipment could be moved back to PLANNED and the analytics module
 *    would then report an actual arrival on a shipment that had not shipped.
 *
 * 2. Bypassing the dispatch gates — DISPATCHED is deliberately unreachable here.
 *    It is only set by dispatchShipment(), which first checks the pick list,
 *    documents, carrier status and compliance flags. Allowing PATCH
 *    /status?status=DISPATCHED would let a coordinator skip all four.
 */
public final class ShipmentStatusTransitions {

    private static final Map<ShipmentStatus, Set<ShipmentStatus>> ALLOWED =
            new EnumMap<>(ShipmentStatus.class);

    static {
        // Pre-dispatch: warehouse readiness progresses, or something goes wrong.
        ALLOWED.put(ShipmentStatus.PLANNED, EnumSet.of(
                ShipmentStatus.AWAITING_PICKING,
                ShipmentStatus.READY_FOR_DISPATCH,
                ShipmentStatus.EXCEPTION));

        ALLOWED.put(ShipmentStatus.AWAITING_PICKING, EnumSet.of(
                ShipmentStatus.PLANNED,
                ShipmentStatus.READY_FOR_DISPATCH,
                ShipmentStatus.EXCEPTION));

        ALLOWED.put(ShipmentStatus.READY_FOR_DISPATCH, EnumSet.of(
                ShipmentStatus.AWAITING_PICKING,
                ShipmentStatus.EXCEPTION));

        // In flight.
        ALLOWED.put(ShipmentStatus.DISPATCHED, EnumSet.of(
                ShipmentStatus.INTRANSIT,
                ShipmentStatus.DELAYED,
                ShipmentStatus.DELIVERED,
                ShipmentStatus.EXCEPTION));

        ALLOWED.put(ShipmentStatus.INTRANSIT, EnumSet.of(
                ShipmentStatus.DELAYED,
                ShipmentStatus.DELIVERED,
                ShipmentStatus.EXCEPTION));

        ALLOWED.put(ShipmentStatus.DELAYED, EnumSet.of(
                ShipmentStatus.INTRANSIT,
                ShipmentStatus.DELIVERED,
                ShipmentStatus.EXCEPTION));

        // An exception is recoverable: the shipment can resume or still arrive.
        ALLOWED.put(ShipmentStatus.EXCEPTION, EnumSet.of(
                ShipmentStatus.INTRANSIT,
                ShipmentStatus.DELAYED,
                ShipmentStatus.DELIVERED));

        // Terminal.
        ALLOWED.put(ShipmentStatus.DELIVERED, EnumSet.noneOf(ShipmentStatus.class));
    }

    private ShipmentStatusTransitions() {
    }

    public static boolean isAllowed(ShipmentStatus from, ShipmentStatus to) {
        if (from == null || to == null) {
            return false;
        }

        // Re-applying the current status is a harmless no-op.
        if (from == to) {
            return true;
        }

        return ALLOWED.getOrDefault(from, EnumSet.noneOf(ShipmentStatus.class)).contains(to);
    }

    public static Set<ShipmentStatus> allowedFrom(ShipmentStatus from) {
        if (from == null) {
            return EnumSet.noneOf(ShipmentStatus.class);
        }

        return ALLOWED.getOrDefault(from, EnumSet.noneOf(ShipmentStatus.class));
    }

    /** Explains a rejected transition in terms an operator can act on. */
    public static String describeRejection(ShipmentStatus from, ShipmentStatus to) {
        if (to == ShipmentStatus.DISPATCHED) {
            return "A shipment cannot be moved to DISPATCHED directly. Use the dispatch action,"
                    + " which verifies the pick list, documents, carrier status and compliance flags.";
        }

        if (from == ShipmentStatus.DELIVERED) {
            return "Shipment is already DELIVERED, which is a final status. It cannot be changed to "
                    + to + ".";
        }

        Set<ShipmentStatus> allowed = allowedFrom(from);

        if (allowed.isEmpty()) {
            return "No status change is allowed from " + from + ".";
        }

        return "Cannot change status from " + from + " to " + to + ". Allowed next statuses: "
                + allowed.stream().map(Enum::name).sorted().collect(Collectors.joining(", ")) + ".";
    }
}
