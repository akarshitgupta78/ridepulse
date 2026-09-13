package com.ridepulse.matching.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridepulse.matching.dto.RideRequestedEvent;
import com.ridepulse.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideRequestedConsumer {

    private final MatchingService matchingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "ride.events", groupId = "matching-engine-group")
    public void consumeRideEvent(String message) {
        try {
            RideRequestedEvent event = objectMapper.readValue(message, RideRequestedEvent.class);

            if ("RIDE_REQUESTED".equalsIgnoreCase(event.getEventType())) {
                log.info("Received RIDE_REQUESTED event for rideId: {}", event.getRideId());
                matchingService.processRideMatching(
                        event.getRideId(),
                        event.getPayload().getPickupLatitude(),
                        event.getPayload().getPickupLongitude()
                );
            }
        } catch (Exception e) {
            log.error("Error processing message from Kafka: {}", message, e);
        }
    }
}
