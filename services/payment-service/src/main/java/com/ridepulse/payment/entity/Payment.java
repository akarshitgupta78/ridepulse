package com.ridepulse.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String rideId;

    @Column(nullable = false)
    private String riderId;

    private String driverId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status; // "SUCCESS", "FAILED", "REFUNDED"

    private String transactionRef;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime processedAt;
}
