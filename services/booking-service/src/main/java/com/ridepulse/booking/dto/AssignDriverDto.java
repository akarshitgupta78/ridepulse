package com.ridepulse.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignDriverDto {
    @NotBlank
    private String driverId;
}
