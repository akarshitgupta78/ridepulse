package com.ridepulse.location.controller;

import com.ridepulse.location.dto.DriverLocationDto;
import com.ridepulse.location.service.DriverLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class DriverLocationController {

    private final DriverLocationService driverLocationService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/driver.location")
    public void receiveLocationPing(@Payload DriverLocationDto locationDto) {
        driverLocationService.updateDriverLocation(locationDto);
        messagingTemplate.convertAndSend("/topic/driver." + locationDto.getDriverId(), locationDto);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<GeoResult<RedisGeoCommands.GeoLocation<Object>>>> getNearbyDrivers(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5.0") double radiusKm) {
        return ResponseEntity.ok(driverLocationService.findNearbyDrivers(lat, lng, radiusKm));
    }
}