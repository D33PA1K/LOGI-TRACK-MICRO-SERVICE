package com.cognizant.logitrack.exception;

import feign.FeignException;

/**
 * Classifies a Feign failure inside a fallback so each operation can report a
 * meaningful, distinct error:
 *  - a downstream 4xx (real bad input / not found) becomes a 400 carrying the
 *    operation's badInput message;
 *  - anything else (connection refused, timeout, 5xx, open circuit) becomes a
 *    503 carrying the operation's "service unavailable" message.
 */
public final class FeignErrorSupport {

    private FeignErrorSupport() {
    }

    public static RuntimeException translate(Throwable cause,
                                             String badInputMessage,
                                             String unavailableMessage) {
        if (cause instanceof FeignException fe
                && fe.status() >= 400 && fe.status() < 500) {
            return new BadRequestException(badInputMessage);
        }
        return new ServiceUnavailableException(unavailableMessage);
    }
}
