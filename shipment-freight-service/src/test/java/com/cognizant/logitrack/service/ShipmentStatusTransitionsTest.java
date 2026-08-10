package com.cognizant.logitrack.service;

import com.cognizant.logitrack.enums.ShipmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class ShipmentStatusTransitionsTest {

    @ParameterizedTest
    @EnumSource(ShipmentStatus.class)
    @DisplayName("DISPATCHED is unreachable from every status, so the dispatch gates cannot be bypassed")
    void dispatchedIsNeverReachableViaAStatusUpdate(ShipmentStatus from) {
        if (from == ShipmentStatus.DISPATCHED) {
            // Re-applying the current status is an allowed no-op.
            assertTrue(ShipmentStatusTransitions.isAllowed(from, ShipmentStatus.DISPATCHED));
            return;
        }

        assertFalse(ShipmentStatusTransitions.isAllowed(from, ShipmentStatus.DISPATCHED),
                "PATCH /status must not be able to reach DISPATCHED from " + from
                        + " — that would skip the pick list, document, carrier and compliance gates");
    }

    @Test
    @DisplayName("the rejection message points the caller at the dispatch action")
    void dispatchRejectionExplainsWhy() {
        String message = ShipmentStatusTransitions.describeRejection(
                ShipmentStatus.PLANNED, ShipmentStatus.DISPATCHED);

        assertTrue(message.contains("dispatch action"), message);
    }

    @ParameterizedTest
    @EnumSource(ShipmentStatus.class)
    @DisplayName("DELIVERED is terminal: no status may follow it")
    void deliveredIsTerminal(ShipmentStatus to) {
        if (to == ShipmentStatus.DELIVERED) {
            assertTrue(ShipmentStatusTransitions.isAllowed(ShipmentStatus.DELIVERED, to));
            return;
        }

        assertFalse(ShipmentStatusTransitions.isAllowed(ShipmentStatus.DELIVERED, to),
                "A DELIVERED shipment must not be able to move to " + to);
    }

    @Test
    @DisplayName("a delivered shipment cannot be rewound to PLANNED")
    void deliveredCannotBeRewound() {
        assertFalse(ShipmentStatusTransitions.isAllowed(
                ShipmentStatus.DELIVERED, ShipmentStatus.PLANNED));

        assertTrue(ShipmentStatusTransitions
                .describeRejection(ShipmentStatus.DELIVERED, ShipmentStatus.PLANNED)
                .contains("final status"));
    }

    @Test
    @DisplayName("the normal in-flight progression is allowed")
    void happyPathInFlightTransitions() {
        assertTrue(ShipmentStatusTransitions.isAllowed(
                ShipmentStatus.DISPATCHED, ShipmentStatus.INTRANSIT));
        assertTrue(ShipmentStatusTransitions.isAllowed(
                ShipmentStatus.INTRANSIT, ShipmentStatus.DELIVERED));
        assertTrue(ShipmentStatusTransitions.isAllowed(
                ShipmentStatus.INTRANSIT, ShipmentStatus.DELAYED));
        assertTrue(ShipmentStatusTransitions.isAllowed(
                ShipmentStatus.DELAYED, ShipmentStatus.DELIVERED));
    }

    @Test
    @DisplayName("an exception is recoverable — the shipment can resume or still arrive")
    void exceptionIsRecoverable() {
        assertTrue(ShipmentStatusTransitions.isAllowed(
                ShipmentStatus.EXCEPTION, ShipmentStatus.INTRANSIT));
        assertTrue(ShipmentStatusTransitions.isAllowed(
                ShipmentStatus.EXCEPTION, ShipmentStatus.DELIVERED));
    }

    @Test
    @DisplayName("a shipment cannot be delivered before it has been dispatched")
    void cannotDeliverBeforeDispatch() {
        assertFalse(ShipmentStatusTransitions.isAllowed(
                ShipmentStatus.PLANNED, ShipmentStatus.DELIVERED));
        assertFalse(ShipmentStatusTransitions.isAllowed(
                ShipmentStatus.READY_FOR_DISPATCH, ShipmentStatus.DELIVERED));
    }

    @Test
    @DisplayName("null statuses are rejected rather than throwing")
    void nullsAreRejected() {
        assertFalse(ShipmentStatusTransitions.isAllowed(null, ShipmentStatus.DELIVERED));
        assertFalse(ShipmentStatusTransitions.isAllowed(ShipmentStatus.PLANNED, null));
        assertTrue(ShipmentStatusTransitions.allowedFrom(null).isEmpty());
    }

    @Test
    @DisplayName("the rejection message lists what the caller could do instead")
    void rejectionListsTheAllowedAlternatives() {
        String message = ShipmentStatusTransitions.describeRejection(
                ShipmentStatus.PLANNED, ShipmentStatus.DELIVERED);

        assertTrue(message.contains("AWAITING_PICKING"), message);
        assertTrue(message.contains("READY_FOR_DISPATCH"), message);
    }
}
