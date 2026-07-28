package com.skypath.backend.exception;

public class InvalidAirportCodeException extends RuntimeException {
    public InvalidAirportCodeException(String message) {
        super(message);
    }
}
