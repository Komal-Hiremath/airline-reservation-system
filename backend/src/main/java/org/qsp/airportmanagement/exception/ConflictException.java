package org.qsp.airportmanagement.exception;

/** Base class for every error that is reported as HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
