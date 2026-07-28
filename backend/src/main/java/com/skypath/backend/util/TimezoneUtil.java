package com.skypath.backend.util;

import com.skypath.backend.model.Airport;
import com.skypath.backend.model.Flight;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimezoneUtil {

    /**
     * Calculate the actual duration of a flight in minutes, accounting for time zones
     */
    public static long calculateFlightDuration(Flight flight, Airport originAirport, Airport destAirport) {
        ZoneId originZone = ZoneId.of(originAirport.getTimezone());
        ZoneId destZone = ZoneId.of(destAirport.getTimezone());

        ZonedDateTime departureZoned = ZonedDateTime.of(flight.getDepartureTime(), originZone);
        ZonedDateTime arrivalZoned = ZonedDateTime.of(flight.getArrivalTime(), destZone);

        return Duration.between(departureZoned, arrivalZoned).toMinutes();
    }

    /**
     * Calculate layover duration between two flights in minutes
     */
    public static long calculateLayoverDuration(Flight arrivalFlight, Flight departureFlight,
                                                  Airport connectionAirport) {
        ZoneId connectionZone = ZoneId.of(connectionAirport.getTimezone());

        ZonedDateTime arrival = ZonedDateTime.of(arrivalFlight.getArrivalTime(), connectionZone);
        ZonedDateTime departure = ZonedDateTime.of(departureFlight.getDepartureTime(), connectionZone);

        return Duration.between(arrival, departure).toMinutes();
    }

    /**
     * Calculate total duration from first departure to last arrival in minutes
     */
    public static long calculateTotalDuration(LocalDateTime firstDeparture, Airport firstAirport,
                                               LocalDateTime lastArrival, Airport lastAirport) {
        ZoneId firstZone = ZoneId.of(firstAirport.getTimezone());
        ZoneId lastZone = ZoneId.of(lastAirport.getTimezone());

        ZonedDateTime departureZoned = ZonedDateTime.of(firstDeparture, firstZone);
        ZonedDateTime arrivalZoned = ZonedDateTime.of(lastArrival, lastZone);

        return Duration.between(departureZoned, arrivalZoned).toMinutes();
    }
}
