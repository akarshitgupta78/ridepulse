package com.ridepulse.location.service;

import com.ridepulse.location.dto.DriverLocationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverLocationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String DRIVER_GEO_KEY = "active_drivers:geo";
    private static final String DRIVER_META_KEY_PREFIX = "driver:meta:";

    public void updateDriverLocation(DriverLocationDto dto) {
        redisTemplate.opsForGeo().add(
                DRIVER_GEO_KEY,
                new Point(dto.getLongitude(), dto.getLatitude()),
                dto.getDriverId()
        );

        String metaKey = DRIVER_META_KEY_PREFIX + dto.getDriverId();
        redisTemplate.opsForValue().set(metaKey, dto, Duration.ofSeconds(30));

        log.debug("Updated location for driver: {} [{}, {}]", dto.getDriverId(), dto.getLatitude(), dto.getLongitude());
    }

    public List<GeoResult<RedisGeoCommands.GeoLocation<Object>>> findNearbyDrivers(double latitude, double longitude, double radiusKm) {
        Circle searchArea = new Circle(new Point(longitude, latitude), new Distance(radiusKm, Metrics.KILOMETERS));
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending();

        GeoResults<RedisGeoCommands.GeoLocation<Object>> results = redisTemplate.opsForGeo().radius(DRIVER_GEO_KEY, searchArea, args);
        return results != null ? results.getContent() : Collections.emptyList();
    }
}