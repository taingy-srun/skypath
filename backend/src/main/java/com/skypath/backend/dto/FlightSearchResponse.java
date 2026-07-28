package com.skypath.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSearchResponse {
    private String origin;
    private String destination;
    private String date;
    private int totalResults;
    private List<Itinerary> itineraries;
}
