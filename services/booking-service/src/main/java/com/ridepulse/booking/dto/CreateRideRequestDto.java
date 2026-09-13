package com.ridepulse.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRideRequestDto {
    @NotBlank
    private String riderId;
    @NotNull
    private Double pickupLat;
    @NotNull
    private Double pickupLng;
    @NotNull
    private Double dropoffLat;
    @NotNull
    private Double dropoffLng;
    private BigDecimal estimatedFare;
}
