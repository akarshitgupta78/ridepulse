package com.ridepulse.matching.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${services.booking-service.url}")
    private String bookingServiceUrl;

    public boolean assignDriverToRide(String rideId, String driverId) {
        try {
            String url = bookingServiceUrl + "/" + rideId + "/assign";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of("driverId", driverId), headers);
            restTemplate.put(url, request);
            log.info("Successfully linked driver {} to ride {}", driverId, rideId);
            return true;
        } catch (Exception e) {
            log.error("Failed to assign driver {} to ride {}: {}", driverId, rideId, e.getMessage());
            return false;
        }
    }
}
