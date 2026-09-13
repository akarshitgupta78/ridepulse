package com.ridepulse.driver.service;

import com.ridepulse.driver.dto.DriverDto;
import com.ridepulse.driver.entity.Driver;
import com.ridepulse.driver.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    public Driver registerDriver(DriverDto dto) {
        if (driverRepository.findByPhone(dto.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Driver with phone already registered: " + dto.getPhone());
        }

        Driver driver = Driver.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .vehicleNumber(dto.getVehicleNumber())
                .vehicleModel(dto.getVehicleModel())
                .isOnline(true)
                .build();

        return driverRepository.save(driver);
    }

    public Driver setStatus(String driverId, boolean isOnline) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));
        driver.setOnline(isOnline);
        return driverRepository.save(driver);
    }

    public Driver getDriver(String driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));
    }
}
