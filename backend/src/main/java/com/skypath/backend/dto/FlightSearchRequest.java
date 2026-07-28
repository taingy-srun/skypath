package com.skypath.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchRequest {
    private String origin;
    private String destination;
    private String date;
}
