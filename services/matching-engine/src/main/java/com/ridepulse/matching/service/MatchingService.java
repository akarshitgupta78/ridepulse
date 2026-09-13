package com.ridepulse.matching.service;

import com.ridepulse.matching.client.BookingServiceClient;
import com.ridepulse.matching.dto.DriverCandidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final BookingServiceClient bookingServiceClient;

    private static final String DRIVER_GEO_KEY = "active_drivers:geo";

    public void processRideMatching(String rideId, double pickupLat, double pickupLng) {
        log.info("Starting driver matching for rideId: {} at [{}, {}]", rideId, pickupLat, pickupLng);

        // 1. Fetch nearby available drivers within 5km from Redis Geo
        Circle searchArea = new Circle(new Point(pickupLng, pickupLat), new Distance(5.0, Metrics.KILOMETERS));
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .sortAscending();

        GeoResults<RedisGeoCommands.GeoLocation<Object>> geoResults = redisTemplate.opsForGeo().radius(DRIVER_GEO_KEY, searchArea, args);

        if (geoResults == null || geoResults.getContent().isEmpty()) {
            log.warn("No active drivers found within 5km for rideId: {}", rideId);
            return;
        }

        // 2. Score candidates using heuristic formula
        List<DriverCandidate> candidates = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<Object>> result : geoResults) {
            String driverId = result.getContent().getName().toString();
            double distance = result.getDistance().getValue();
            double eta = distance * 3.0; // Rough heuristic: 3 mins per km
            double rating = 4.8;         // Default rating placeholder
            double acceptanceRate = 0.95;

            // Priority Score formula: lower is better
            double score = (0.4 * distance) + (0.3 * eta) - (0.2 * rating) - (0.1 * acceptanceRate);

            candidates.add(DriverCandidate.builder()
                    .driverId(driverId)
                    .distanceKm(distance)
                    .etaMinutes(eta)
                    .rating(rating)
                    .acceptanceRate(acceptanceRate)
                    .score(score)
                    .build());
        }

        candidates.sort(Comparator.comparingDouble(DriverCandidate::getScore));

        // 3. Iterate candidates & acquire distributed lock to avoid double booking
        for (DriverCandidate candidate : candidates) {
            String lockKey = "lock:driver:" + candidate.getDriverId();
            RLock lock = redissonClient.getLock(lockKey);

            try {
                // Try locking driver for 15 seconds
                boolean isLocked = lock.tryLock(500, 15000, TimeUnit.MILLISECONDS);
                if (isLocked) {
                    log.info("Acquired lock for driver: {}. Dispatching ride: {}", candidate.getDriverId(), rideId);

                    boolean assigned = bookingServiceClient.assignDriverToRide(rideId, candidate.getDriverId());
                    if (assigned) {
                        // Remove driver from available geo pool while on ride
                        redisTemplate.opsForGeo().remove(DRIVER_GEO_KEY, candidate.getDriverId());
                        return; // Match completed successfully
                    } else {
                        lock.unlock(); // Release lock if assignment fails
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Lock acquisition interrupted for driver {}", candidate.getDriverId());
            }
        }

        log.warn("Failed to assign any candidate driver for rideId: {}", rideId);
    }
}
