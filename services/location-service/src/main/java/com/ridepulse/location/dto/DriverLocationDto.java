package com.ridepulse.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocationDto {
    private String driverId;
    private double latitude;
    private double longitude;
    private double bearing;
    private String status; // "AVAILABLE", "ON_RIDE", "OFFLINE"
    private long timestamp;
}