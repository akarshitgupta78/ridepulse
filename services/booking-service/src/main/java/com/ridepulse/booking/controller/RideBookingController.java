package com.ridepulse.booking.controller;

import com.ridepulse.booking.dto.AssignDriverDto;
import com.ridepulse.booking.dto.CreateRideRequestDto;
import com.ridepulse.booking.entity.Ride;
import com.ridepulse.booking.service.RideBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideBookingController {

    private final RideBookingService rideBookingService;

    @PostMapping("/request")
    public ResponseEntity<Ride> requestRide(@Valid @RequestBody CreateRideRequestDto requestDto) {
        Ride ride = rideBookingService.requestRide(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ride);
    }

    @PutMapping("/{rideId}/assign")
    public ResponseEntity<Ride> assignDriver(@PathVariable String rideId, @Valid @RequestBody AssignDriverDto dto) {
        return ResponseEntity.ok(rideBookingService.assignDriver(rideId, dto));
    }

    @PutMapping("/{rideId}/complete")
    public ResponseEntity<Ride> completeRide(@PathVariable String rideId) {
        return ResponseEntity.ok(rideBookingService.completeRide(rideId));
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<Ride> getRide(@PathVariable String rideId) {
        return ResponseEntity.ok(rideBookingService.getRideById(rideId));
    }
}
