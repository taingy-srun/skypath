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
public class Itinerary {
    private List<FlightSegment> segments;
    private List<Long> layoverDurationsMinutes;
    private long totalDurationMinutes;
    private double totalPrice;
}
