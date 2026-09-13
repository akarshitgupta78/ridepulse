package com.ridepulse.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPayload {
    private String eventType;
    private String title;
    private String message;
    private String rideId;
    private String recipientId;
    private Object data;
    private long timestamp;
}
