package com.skypath.backend.validator;

import com.skypath.backend.model.Airport;
import com.skypath.backend.model.Flight;
import com.skypath.backend.util.TimezoneUtil;

public class ConnectionValidator {
    private static final long MIN_DOMESTIC_LAYOVER_MINUTES = 45;
    private static final long MIN_INTERNATIONAL_LAYOVER_MINUTES = 90;
    private static final long MAX_LAYOVER_MINUTES = 360; // 6 hours

    /**
     * Validate if two flights can be connected
     */
    public static boolean isValidConnection(Flight arrivalFlight, Flight departureFlight,
                                              Airport connectionAirport,
                                              Airport arrivalOriginAirport,
                                              Airport departureDestAirport) {
        // Check if the connection airport is the same (no airport changes)
        if (!arrivalFlight.getDestination().equals(departureFlight.getOrigin())) {
            return false;
        }

        // Calculate layover duration
        long layoverMinutes = TimezoneUtil.calculateLayoverDuration(arrivalFlight, departureFlight, connectionAirport);

        // Check if layover is negative (departure before arrival)
        if (layoverMinutes < 0) {
            return false;
        }

        boolean isDomestic = isDomesticConnection(arrivalOriginAirport, connectionAirport, departureDestAirport);
        long minLayover = isDomestic ? MIN_DOMESTIC_LAYOVER_MINUTES : MIN_INTERNATIONAL_LAYOVER_MINUTES;

        // Check layover constraints
        return layoverMinutes >= minLayover && layoverMinutes <= MAX_LAYOVER_MINUTES;
    }


    private static boolean isDomesticConnection(Airport... airports) {
        if (airports.length == 0) {
            return false;
        }

        String firstCountry = airports[0].getCountry();
        for (Airport airport : airports) {
            if (!airport.getCountry().equals(firstCountry)) {
                return false;
            }
        }
        return true;
    }
}
