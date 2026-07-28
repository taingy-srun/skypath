package com.skypath.backend.service;

import com.skypath.backend.dto.FlightSegment;
import com.skypath.backend.dto.Itinerary;
import com.skypath.backend.model.Airport;
import com.skypath.backend.model.Flight;
import com.skypath.backend.validator.ConnectionValidator;
import com.skypath.backend.util.TimezoneUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FlightSearchService {
    private final DataLoaderService dataLoaderService;

    public FlightSearchService(DataLoaderService dataLoaderService) {
        this.dataLoaderService = dataLoaderService;
    }

    public List<Itinerary> searchFlights(String origin, String destination, String dateStr) {
        LocalDate searchDate = LocalDate.parse(dateStr);
        List<Itinerary> allItineraries = new ArrayList<>();

        // Find direct flights
        allItineraries.addAll(findDirectFlights(origin, destination, searchDate));

        // Find 1-stop connections
        allItineraries.addAll(findOneStopFlights(origin, destination, searchDate));

        // Find 2-stop connections
        allItineraries.addAll(findTwoStopFlights(origin, destination, searchDate));

        // Sort by total duration (shortest first)
        allItineraries.sort(Comparator.comparingLong(Itinerary::getTotalDurationMinutes));

        return allItineraries;
    }

    private List<Itinerary> findDirectFlights(String origin, String destination, LocalDate date) {
        List<Itinerary> itineraries = new ArrayList<>();

        List<Flight> directFlights = dataLoaderService.getFlights().stream()
                .filter(f -> f.getOrigin().equals(origin) &&
                        f.getDestination().equals(destination) &&
                        f.getDepartureTime().toLocalDate().equals(date))
                .toList();

        for (Flight flight : directFlights) {
            Airport originAirport = dataLoaderService.getAirport(origin);
            Airport destAirport = dataLoaderService.getAirport(destination);

            FlightSegment segment = createFlightSegment(flight, originAirport, destAirport);

            long totalDuration = TimezoneUtil.calculateTotalDuration(
                    flight.getDepartureTime(), originAirport,
                    flight.getArrivalTime(), destAirport
            );

            Itinerary itinerary = Itinerary.builder()
                    .segments(List.of(segment))
                    .layoverDurationsMinutes(List.of())
                    .totalDurationMinutes(totalDuration)
                    .totalPrice(flight.getPrice())
                    .build();

            itineraries.add(itinerary);
        }

        return itineraries;
    }

    private List<Itinerary> findOneStopFlights(String origin, String destination, LocalDate date) {
        List<Itinerary> itineraries = new ArrayList<>();

        List<Flight> firstLegFlights = dataLoaderService.getFlights().stream()
                .filter(f -> f.getOrigin().equals(origin) &&
                        f.getDepartureTime().toLocalDate().equals(date))
                .toList();

        for (Flight firstFlight : firstLegFlights) {
            String connectionAirport = firstFlight.getDestination();

            // Skip if connection is the final destination
            if (connectionAirport.equals(destination)) {
                continue;
            }

            List<Flight> secondLegFlights = dataLoaderService.getFlights().stream()
                    .filter(f -> f.getOrigin().equals(connectionAirport) &&
                            f.getDestination().equals(destination))
                    .toList();

            for (Flight secondFlight : secondLegFlights) {
                Airport originAirport = dataLoaderService.getAirport(origin);
                Airport connAirport = dataLoaderService.getAirport(connectionAirport);
                Airport destAirport = dataLoaderService.getAirport(destination);

                // Validate connection
                if (!ConnectionValidator.isValidConnection(
                        firstFlight, secondFlight, connAirport, originAirport, destAirport)) {
                    continue;
                }

                FlightSegment segment1 = createFlightSegment(firstFlight, originAirport, connAirport);
                FlightSegment segment2 = createFlightSegment(secondFlight, connAirport, destAirport);

                long layover = TimezoneUtil.calculateLayoverDuration(firstFlight, secondFlight, connAirport);

                long totalDuration = TimezoneUtil.calculateTotalDuration(
                        firstFlight.getDepartureTime(), originAirport,
                        secondFlight.getArrivalTime(), destAirport
                );

                Itinerary itinerary = Itinerary.builder()
                        .segments(List.of(segment1, segment2))
                        .layoverDurationsMinutes(List.of(layover))
                        .totalDurationMinutes(totalDuration)
                        .totalPrice(firstFlight.getPrice() + secondFlight.getPrice())
                        .build();

                itineraries.add(itinerary);
            }
        }

        return itineraries;
    }

    private List<Itinerary> findTwoStopFlights(String origin, String destination, LocalDate date) {
        List<Itinerary> itineraries = new ArrayList<>();

        List<Flight> firstLegFlights = dataLoaderService.getFlights().stream()
                .filter(f -> f.getOrigin().equals(origin) &&
                        f.getDepartureTime().toLocalDate().equals(date))
                .toList();

        for (Flight firstFlight : firstLegFlights) {
            String firstConnection = firstFlight.getDestination();

            // Skip if first connection is the final destination
            if (firstConnection.equals(destination)) {
                continue;
            }

            List<Flight> secondLegFlights = dataLoaderService.getFlights().stream()
                    .filter(f -> f.getOrigin().equals(firstConnection))
                    .toList();

            for (Flight secondFlight : secondLegFlights) {
                String secondConnection = secondFlight.getDestination();

                // Skip if second connection is origin or final destination
                if (secondConnection.equals(origin) || secondConnection.equals(destination)) {
                    continue;
                }

                List<Flight> thirdLegFlights = dataLoaderService.getFlights().stream()
                        .filter(f -> f.getOrigin().equals(secondConnection) &&
                                f.getDestination().equals(destination))
                        .toList();

                for (Flight thirdFlight : thirdLegFlights) {
                    Airport originAirport = dataLoaderService.getAirport(origin);
                    Airport firstConnAirport = dataLoaderService.getAirport(firstConnection);
                    Airport secondConnAirport = dataLoaderService.getAirport(secondConnection);
                    Airport destAirport = dataLoaderService.getAirport(destination);

                    // Validate first connection
                    if (!ConnectionValidator.isValidConnection(
                            firstFlight, secondFlight, firstConnAirport,
                            originAirport, secondConnAirport)) {
                        continue;
                    }

                    // Validate second connection
                    if (!ConnectionValidator.isValidConnection(
                            secondFlight, thirdFlight, secondConnAirport,
                            firstConnAirport, destAirport)) {
                        continue;
                    }

                    FlightSegment segment1 = createFlightSegment(firstFlight, originAirport, firstConnAirport);
                    FlightSegment segment2 = createFlightSegment(secondFlight, firstConnAirport, secondConnAirport);
                    FlightSegment segment3 = createFlightSegment(thirdFlight, secondConnAirport, destAirport);

                    long layover1 = TimezoneUtil.calculateLayoverDuration(firstFlight, secondFlight, firstConnAirport);
                    long layover2 = TimezoneUtil.calculateLayoverDuration(secondFlight, thirdFlight, secondConnAirport);

                    long totalDuration = TimezoneUtil.calculateTotalDuration(
                            firstFlight.getDepartureTime(), originAirport,
                            thirdFlight.getArrivalTime(), destAirport
                    );

                    double totalPrice = firstFlight.getPrice() + secondFlight.getPrice() + thirdFlight.getPrice();

                    Itinerary itinerary = Itinerary.builder()
                            .segments(List.of(segment1, segment2, segment3))
                            .layoverDurationsMinutes(List.of(layover1, layover2))
                            .totalDurationMinutes(totalDuration)
                            .totalPrice(totalPrice)
                            .build();

                    itineraries.add(itinerary);
                }
            }
        }

        return itineraries;
    }

    private FlightSegment createFlightSegment(Flight flight, Airport originAirport, Airport destAirport) {
        long duration = TimezoneUtil.calculateFlightDuration(flight, originAirport, destAirport);

        return FlightSegment.builder()
                .flightNumber(flight.getFlightNumber())
                .airline(flight.getAirline())
                .origin(flight.getOrigin())
                .destination(flight.getDestination())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .price(flight.getPrice())
                .aircraft(flight.getAircraft())
                .durationMinutes(duration)
                .build();
    }
}
