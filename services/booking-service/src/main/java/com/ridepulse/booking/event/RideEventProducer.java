package com.ridepulse.booking.event;

import com.ridepulse.booking.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishRideEvent(String eventType, String rideId, String riderId, String driverId, Object payload) {
        RideLifecycleEvent event = RideLifecycleEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .rideId(rideId)
                .riderId(riderId)
                .driverId(driverId)
                .payload(payload)
                .timestamp(System.currentTimeMillis())
                .build();

        // rideId is used as the Kafka message key to preserve order per ride
        kafkaTemplate.send(KafkaTopicConfig.RIDE_EVENTS_TOPIC, rideId, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Kafka Event Published: {} for rideId: {}", eventType, rideId);
                    } else {
                        log.error("Failed to publish Kafka Event: {} for rideId: {}", eventType, rideId, ex);
                    }
                });
    }
}
