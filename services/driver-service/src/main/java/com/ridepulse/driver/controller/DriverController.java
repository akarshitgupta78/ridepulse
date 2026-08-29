package com.ridepulse.driver.controller;

import com.ridepulse.driver.dto.DriverDto;
import com.ridepulse.driver.entity.Driver;
import com.ridepulse.driver.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping("/register")
    public ResponseEntity<Driver> register(@Valid @RequestBody DriverDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(driverService.registerDriver(dto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Driver> updateStatus(@PathVariable String id, @RequestParam boolean online) {
        return ResponseEntity.ok(driverService.setStatus(id, online));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Driver> getDriver(@PathVariable String id) {
        return ResponseEntity.ok(driverService.getDriver(id));
    }
}
