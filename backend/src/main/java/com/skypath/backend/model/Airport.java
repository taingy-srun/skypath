package com.skypath.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Airport {
    private String code;
    private String name;
    private String city;
    private String country;
    private String timezone;
}
