package org.qsp.airportmanagement.exception;

public class DuplicateEmailException extends ConflictException {

    public DuplicateEmailException(String email) {
        super("Email is already registered: " + email);
    }
}
