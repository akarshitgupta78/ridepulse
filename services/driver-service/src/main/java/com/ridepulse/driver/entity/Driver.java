package com.ridepulse.driver.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false)
    private String vehicleNumber;

    @Column(nullable = false)
    private String vehicleModel;

    @Builder.Default
    private boolean isOnline = false;

    @Builder.Default
    private Double rating = 4.9;

    @Builder.Default
    private Double acceptanceRate = 0.98;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime registeredAt;
}
