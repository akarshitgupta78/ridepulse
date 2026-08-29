package com.ridepulse.payment.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridepulse.payment.service.PaymentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideCompletedConsumer {

    private final PaymentProcessingService paymentProcessingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "ride.events", groupId = "payment-service-group")
    public void consumeRideEvent(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.path("eventType").asText();

            if ("RIDE_COMPLETED".equalsIgnoreCase(eventType)) {
                String rideId = root.path("rideId").asText();
                String riderId = root.path("riderId").asText();
                String driverId = root.path("driverId").asText();
                
                JsonNode payload = root.path("payload");
                double fareDouble = payload.path("finalFare").asDouble(payload.path("estimatedFare").asDouble(20.0));
                BigDecimal fare = BigDecimal.valueOf(fareDouble);

                log.info("Received RIDE_COMPLETED event for ride: {}", rideId);
                paymentProcessingService.processPaymentForCompletedRide(rideId, riderId, driverId, fare);
            }
        } catch (Exception e) {
            log.error("Failed to parse/process Kafka ride event: {}", message, e);
        }
    }
}
