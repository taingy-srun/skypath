package com.skypath.backend.validator;

import com.skypath.backend.dto.FlightSearchRequest;
import com.skypath.backend.exception.InvalidAirportCodeException;
import com.skypath.backend.exception.InvalidSearchParametersException;
import com.skypath.backend.model.Airport;
import com.skypath.backend.service.DataLoaderService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
public class SearchParamsValidator {

    private final DataLoaderService dataLoaderService;

    public SearchParamsValidator(DataLoaderService dataLoaderService) {
        this.dataLoaderService = dataLoaderService;
    }

    /**
     * Validate FlightSearchRequest object
     */
    public void validate(FlightSearchRequest request) {
        if (request == null) {
            throw new InvalidSearchParametersException("Invalid search parameters");
        }
        validate(request.getOrigin(), request.getDestination(), request.getDate());
    }

    /**
     * Validate individual search parameters
     */
    public void validate(String origin, String destination, String date) {
        // Check if origin and destination are the same
        if (origin != null && destination != null &&
                origin.trim().equalsIgnoreCase(destination.trim())) {
            throw new InvalidAirportCodeException("Origin and destination cannot be the same.");
        }

        validateAirportCode(origin, "origin");

        validateAirportCode(destination, "destination");

        validateDate(date);
    }

    private void validateAirportCode(String code, String fieldName) {
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidAirportCodeException(fieldName + " airport code is required.");
        } else if (code.length() != 3) {
            throw new InvalidAirportCodeException("Airport code must be 3 characters.");
        } else {
            Airport airport = dataLoaderService.getAirport(code.toUpperCase());
            if (airport == null) {
                throw new InvalidAirportCodeException("Invalid " + fieldName + " airport code: " + code.toUpperCase());
            }
        }
    }

    private void validateDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            throw new InvalidSearchParametersException("Date is required.");
        }
        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new InvalidSearchParametersException("Invalid date format. Please use YYYY-MM-DD");
        }
    }
}
