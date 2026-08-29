package com.ridepulse.booking.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideLifecycleEvent {
    private String eventId;
    private String eventType; // e.g., RIDE_REQUESTED, DRIVER_ASSIGNED, RIDE_COMPLETED
    private String rideId;
    private String riderId;
    private String driverId;
    private Object payload;
    private long timestamp;
}
