package com.ridepulse.notification.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridepulse.notification.dto.NotificationPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "ride.events", groupId = "notification-service-group")
    public void consumeRideEvent(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.path("eventType").asText();
            String rideId = root.path("rideId").asText();
            String riderId = root.path("riderId").asText();
            String driverId = root.path("driverId").asText();

            log.info("Notification Service received event: {} for ride: {}", eventType, rideId);

            switch (eventType) {
                case "DRIVER_ASSIGNED" -> {
                    NotificationPayload riderAlert = NotificationPayload.builder()
                            .eventType(eventType)
                            .title("Driver Found!")
                            .message("A driver has accepted your ride request and is on their way.")
                            .rideId(rideId)
                            .recipientId(riderId)
                            .timestamp(System.currentTimeMillis())
                            .build();

                    // Push to rider's dedicated topic
                    messagingTemplate.convertAndSend("/topic/rider." + riderId, riderAlert);
                }
                case "RIDE_COMPLETED" -> {
                    NotificationPayload completionAlert = NotificationPayload.builder()
                            .eventType(eventType)
                            .title("Ride Completed")
                            .message("You have reached your destination. Thank you for riding with RidePulse!")
                            .rideId(rideId)
                            .recipientId(riderId)
                            .timestamp(System.currentTimeMillis())
                            .build();

                    messagingTemplate.convertAndSend("/topic/rider." + riderId, completionAlert);
                    if (!driverId.isEmpty()) {
                        messagingTemplate.convertAndSend("/topic/driver." + driverId, completionAlert);
                    }
                }
                default -> log.debug("Event {} ignored by notification dispatcher", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process event for notification delivery: {}", message, e);
        }
    }
}
