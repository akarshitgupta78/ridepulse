package com.ridepulse.matching.dto;

import lombok.Data;

@Data
public class RideRequestedEvent {
    private String eventId;
    private String eventType;
    private String rideId;
    private String riderId;
    private RidePayload payload;

    @Data
    public static class RidePayload {
        private String id;
        private String riderId;
        private double pickupLatitude;
        private double pickupLongitude;
        private double dropoffLatitude;
        private double dropoffLongitude;
    }
}
