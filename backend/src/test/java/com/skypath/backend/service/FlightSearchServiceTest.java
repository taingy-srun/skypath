package com.skypath.backend.service;

import com.skypath.backend.dto.Itinerary;
import com.skypath.backend.model.Airport;
import com.skypath.backend.model.Flight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FlightSearchServiceTest {

    @Mock
    private DataLoaderService dataLoaderService;

    @InjectMocks
    private FlightSearchService flightSearchService;

    private Airport jfk;
    private Airport lax;
    private Airport ord;
    private Airport sfo;

    @BeforeEach
    void setUp() {
        // Setup airports
        jfk = new Airport("JFK", "John F. Kennedy International Airport",
                "New York", "United States", "America/New_York");

        lax = new Airport("LAX", "Los Angeles International Airport",
                "Los Angeles", "United States", "America/Los_Angeles");

        ord = new Airport("ORD", "O'Hare International Airport",
                "Chicago", "United States", "America/Chicago");

        sfo = new Airport("SFO", "San Francisco International Airport",
                "San Francisco", "United States", "America/Los_Angeles");

        // Setup airport lookups
        when(dataLoaderService.getAirport("JFK")).thenReturn(jfk);
        when(dataLoaderService.getAirport("LAX")).thenReturn(lax);
        when(dataLoaderService.getAirport("ORD")).thenReturn(ord);
        when(dataLoaderService.getAirport("SFO")).thenReturn(sfo);
    }

    @Test
    void searchFlights_shouldReturnDirectFlights() {
        // Given: Direct flight from JFK to LAX
        Flight directFlight = new Flight("AA100", "American Airlines", "JFK", "LAX",
                LocalDateTime.parse("2024-03-15T08:00:00"),
                LocalDateTime.parse("2024-03-15T11:30:00"),
                299.99, "Boeing 737");

        when(dataLoaderService.getFlights()).thenReturn(List.of(directFlight));

        // When: Search for flights
        List<Itinerary> results = flightSearchService.searchFlights("JFK", "LAX", "2024-03-15");

        // Then: Should return 1 direct flight itinerary
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSegments()).hasSize(1);
        assertThat(results.get(0).getSegments().get(0).getFlightNumber()).isEqualTo("AA100");
        assertThat(results.get(0).getTotalPrice()).isEqualTo(299.99);
        assertThat(results.get(0).getLayoverDurationsMinutes()).isEmpty();
    }

    @Test
    void searchFlights_shouldReturnOneStopFlights() {
        // Given: Two flights that can connect (JFK -> ORD -> LAX)
        Flight firstLeg = new Flight("UA200", "United Airlines", "JFK", "ORD",
                LocalDateTime.parse("2024-03-15T08:00:00"),
                LocalDateTime.parse("2024-03-15T10:00:00"),
                199.99, "Boeing 737");

        Flight secondLeg = new Flight("UA201", "United Airlines", "ORD", "LAX",
                LocalDateTime.parse("2024-03-15T12:00:00"),
                LocalDateTime.parse("2024-03-15T14:00:00"),
                249.99, "Boeing 737");

        when(dataLoaderService.getFlights()).thenReturn(List.of(firstLeg, secondLeg));

        // When: Search for flights
        List<Itinerary> results = flightSearchService.searchFlights("JFK", "LAX", "2024-03-15");

        // Then: Should return 1-stop connection
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSegments()).hasSize(2);
        assertThat(results.get(0).getSegments().get(0).getFlightNumber()).isEqualTo("UA200");
        assertThat(results.get(0).getSegments().get(1).getFlightNumber()).isEqualTo("UA201");
        assertThat(results.get(0).getTotalPrice()).isEqualTo(449.98);
        assertThat(results.get(0).getLayoverDurationsMinutes()).hasSize(1);
    }

    @Test
    void searchFlights_shouldReturnTwoStopFlights() {
        // Given: Three flights that can connect (JFK -> ORD -> SFO -> LAX)
        Flight firstLeg = new Flight("UA300", "United Airlines", "JFK", "ORD",
                LocalDateTime.parse("2024-03-15T08:00:00"),
                LocalDateTime.parse("2024-03-15T10:00:00"),
                199.99, "Boeing 737");

        Flight secondLeg = new Flight("UA301", "United Airlines", "ORD", "SFO",
                LocalDateTime.parse("2024-03-15T12:00:00"),
                LocalDateTime.parse("2024-03-15T14:00:00"),
                249.99, "Boeing 737");

        Flight thirdLeg = new Flight("UA302", "United Airlines", "SFO", "LAX",
                LocalDateTime.parse("2024-03-15T16:00:00"),
                LocalDateTime.parse("2024-03-15T17:30:00"),
                149.99, "Boeing 737");

        when(dataLoaderService.getFlights()).thenReturn(List.of(firstLeg, secondLeg, thirdLeg));

        // When: Search for flights
        List<Itinerary> results = flightSearchService.searchFlights("JFK", "LAX", "2024-03-15");

        // Then: Should return 2-stop connection
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSegments()).hasSize(3);
        assertThat(results.get(0).getSegments().get(0).getFlightNumber()).isEqualTo("UA300");
        assertThat(results.get(0).getSegments().get(1).getFlightNumber()).isEqualTo("UA301");
        assertThat(results.get(0).getSegments().get(2).getFlightNumber()).isEqualTo("UA302");
        assertThat(results.get(0).getTotalPrice()).isEqualTo(599.97);
        assertThat(results.get(0).getLayoverDurationsMinutes()).hasSize(2);
    }

    @Test
    void searchFlights_shouldExcludeInvalidConnections() {
        // Given: Two flights with too short layover (less than 45 minutes)
        Flight firstLeg = new Flight("UA400", "United Airlines", "JFK", "ORD",
                LocalDateTime.parse("2024-03-15T08:00:00"),
                LocalDateTime.parse("2024-03-15T10:00:00"),
                199.99, "Boeing 737");

        Flight secondLeg = new Flight("UA401", "United Airlines", "ORD", "LAX",
                LocalDateTime.parse("2024-03-15T10:30:00"), // Only 30 min layover
                LocalDateTime.parse("2024-03-15T12:30:00"),
                249.99, "Boeing 737");

        when(dataLoaderService.getFlights()).thenReturn(List.of(firstLeg, secondLeg));

        // When: Search for flights
        List<Itinerary> results = flightSearchService.searchFlights("JFK", "LAX", "2024-03-15");

        // Then: Should return no results (invalid connection filtered out)
        assertThat(results).isEmpty();
    }

    @Test
    void searchFlights_shouldSortByTotalDuration() {
        // Given: Multiple flights with different durations
        Flight shortFlight = new Flight("AA500", "American Airlines", "JFK", "LAX",
                LocalDateTime.parse("2024-03-15T08:00:00"),
                LocalDateTime.parse("2024-03-15T11:00:00"), // 6 hours total
                399.99, "Boeing 737");

        Flight longFlight = new Flight("AA501", "American Airlines", "JFK", "LAX",
                LocalDateTime.parse("2024-03-15T10:00:00"),
                LocalDateTime.parse("2024-03-15T14:00:00"), // 7 hours total
                299.99, "Boeing 737");

        when(dataLoaderService.getFlights()).thenReturn(List.of(longFlight, shortFlight));

        // When: Search for flights
        List<Itinerary> results = flightSearchService.searchFlights("JFK", "LAX", "2024-03-15");

        // Then: Should be sorted by duration (shortest first)
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getSegments().get(0).getFlightNumber()).isEqualTo("AA500");
        assertThat(results.get(1).getSegments().get(0).getFlightNumber()).isEqualTo("AA501");
    }

    @Test
    void searchFlights_shouldReturnEmptyListWhenNoFlightsFound() {
        // Given: No flights available
        when(dataLoaderService.getFlights()).thenReturn(List.of());

        // When: Search for flights
        List<Itinerary> results = flightSearchService.searchFlights("JFK", "LAX", "2024-03-15");

        // Then: Should return empty list
        assertThat(results).isEmpty();
    }

    @Test
    void searchFlights_shouldNotIncludeFlightsOnDifferentDates() {
        // Given: Flight on different date
        Flight wrongDateFlight = new Flight("AA600", "American Airlines", "JFK", "LAX",
                LocalDateTime.parse("2024-03-16T08:00:00"), // Different date
                LocalDateTime.parse("2024-03-16T11:00:00"),
                299.99, "Boeing 737");

        when(dataLoaderService.getFlights()).thenReturn(List.of(wrongDateFlight));

        // When: Search for flights on different date
        List<Itinerary> results = flightSearchService.searchFlights("JFK", "LAX", "2024-03-15");

        // Then: Should return no results
        assertThat(results).isEmpty();
    }

    @Test
    void searchFlights_shouldCombineDirectAndConnectingFlights() {
        // Given: Both direct and connecting flights available
        Flight directFlight = new Flight("AA700", "American Airlines", "JFK", "LAX",
                LocalDateTime.parse("2024-03-15T08:00:00"),
                LocalDateTime.parse("2024-03-15T11:00:00"),
                399.99, "Boeing 737");

        Flight firstLeg = new Flight("UA700", "United Airlines", "JFK", "ORD",
                LocalDateTime.parse("2024-03-15T09:00:00"),
                LocalDateTime.parse("2024-03-15T11:00:00"),
                199.99, "Boeing 737");

        Flight secondLeg = new Flight("UA701", "United Airlines", "ORD", "LAX",
                LocalDateTime.parse("2024-03-15T13:00:00"),
                LocalDateTime.parse("2024-03-15T15:00:00"),
                249.99, "Boeing 737");

        when(dataLoaderService.getFlights()).thenReturn(List.of(directFlight, firstLeg, secondLeg));

        // When: Search for flights
        List<Itinerary> results = flightSearchService.searchFlights("JFK", "LAX", "2024-03-15");

        // Then: Should return both direct and connecting flights
        assertThat(results).hasSize(2);
        // Verify we have one direct and one connecting
        long directCount = results.stream().filter(i -> i.getSegments().size() == 1).count();
        long connectingCount = results.stream().filter(i -> i.getSegments().size() == 2).count();
        assertThat(directCount).isEqualTo(1);
        assertThat(connectingCount).isEqualTo(1);
    }
}
