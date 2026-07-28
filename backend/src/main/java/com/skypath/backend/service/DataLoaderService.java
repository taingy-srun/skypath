package com.skypath.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.skypath.backend.model.Airport;
import com.skypath.backend.model.Flight;
import com.skypath.backend.model.FlightData;
import lombok.Getter;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataLoaderService {
    private Map<String, Airport> airportMap = new HashMap<>();
    @Getter
    private List<Flight> flights;

    @PostConstruct
    public void loadData() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            File flightsFile = new File("flights.json");
            FlightData flightData = objectMapper.readValue(flightsFile, FlightData.class);

            this.flights = flightData.getFlights();

            for (Airport airport : flightData.getAirports()) {
                airportMap.put(airport.getCode(), airport);
            }

            System.out.println("Loaded " + flights.size() + " flights and " + airportMap.size() + " airports");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load flight data", e);
        }
    }

    public Airport getAirport(String code) {
        return airportMap.get(code);
    }
}
