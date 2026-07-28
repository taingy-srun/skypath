package com.skypath.backend.controller;

import com.skypath.backend.dto.FlightSearchRequest;
import com.skypath.backend.dto.FlightSearchResponse;
import com.skypath.backend.dto.Itinerary;
import com.skypath.backend.service.FlightSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightSearchController {
    private final FlightSearchService flightSearchService;

    public FlightSearchController(FlightSearchService flightSearchService) {
        this.flightSearchService = flightSearchService;
    }

    @PostMapping("/search")
    public ResponseEntity<FlightSearchResponse> searchFlights(@RequestBody FlightSearchRequest request) {

        if (request.getOrigin() == null || request.getDestination() == null || request.getDate() == null) {
            return ResponseEntity.badRequest().build();
        }

        List<Itinerary> itineraries = flightSearchService.searchFlights(
                request.getOrigin().toUpperCase(),
                request.getDestination().toUpperCase(),
                request.getDate()
        );

        FlightSearchResponse response = FlightSearchResponse.builder()
                .origin(request.getOrigin().toUpperCase())
                .destination(request.getDestination().toUpperCase())
                .date(request.getDate())
                .totalResults(itineraries.size())
                .itineraries(itineraries)
                .build();

        return ResponseEntity.ok(response);
    }

}
