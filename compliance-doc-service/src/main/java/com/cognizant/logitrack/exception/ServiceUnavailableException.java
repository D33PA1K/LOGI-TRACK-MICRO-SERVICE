package com.cognizant.logitrack.exception;

/**
 * Thrown when a downstream microservice cannot be reached (connection refused,
 * timeout, 5xx, or an open circuit breaker). Mapped to HTTP 503 so the client
 * can tell "the other service is down" apart from "your input was invalid".
 */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
