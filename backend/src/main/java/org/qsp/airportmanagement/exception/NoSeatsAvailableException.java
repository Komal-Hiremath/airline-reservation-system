package org.qsp.airportmanagement.exception;

public class NoSeatsAvailableException extends ConflictException {

    public NoSeatsAvailableException(int requested, int available) {
        super("Not enough seats: " + requested + " requested, " + available + " available");
    }
}
