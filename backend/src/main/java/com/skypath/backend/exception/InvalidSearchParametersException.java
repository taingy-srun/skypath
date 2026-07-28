package com.skypath.backend.exception;

public class InvalidSearchParametersException extends RuntimeException {
    public InvalidSearchParametersException(String message) {
        super(message);
    }
}
