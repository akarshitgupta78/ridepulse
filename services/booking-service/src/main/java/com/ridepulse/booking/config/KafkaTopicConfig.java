package com.ridepulse.booking.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String RIDE_EVENTS_TOPIC = "ride.events";

    @Bean
    public NewTopic rideEventsTopic() {
        return TopicBuilder.name(RIDE_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
