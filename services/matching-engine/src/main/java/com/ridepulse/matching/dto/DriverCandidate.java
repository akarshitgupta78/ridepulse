package com.ridepulse.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverCandidate {
    private String driverId;
    private double distanceKm;
    private double etaMinutes;
    private double rating;
    private double acceptanceRate;
    private double score; // Lower score = higher dispatch priority
}
