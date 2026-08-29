package com.ridepulse.driver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DriverDto {
    @NotBlank
    private String name;
    @NotBlank
    private String phone;
    @NotBlank
    private String vehicleNumber;
    @NotBlank
    private String vehicleModel;
}
