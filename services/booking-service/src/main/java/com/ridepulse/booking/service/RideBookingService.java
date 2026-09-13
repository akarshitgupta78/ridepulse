package com.ridepulse.booking.service;

import com.ridepulse.booking.dto.AssignDriverDto;
import com.ridepulse.booking.dto.CreateRideRequestDto;
import com.ridepulse.booking.entity.Ride;
import com.ridepulse.booking.entity.RideStatus;
import com.ridepulse.booking.event.RideEventProducer;
import com.ridepulse.booking.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideBookingService {

    private final RideRepository rideRepository;
    private final RideEventProducer rideEventProducer;

    @Transactional
    public Ride requestRide(CreateRideRequestDto dto) {
        Ride ride = Ride.builder()
                .riderId(dto.getRiderId())
                .pickupLatitude(dto.getPickupLat())
                .pickupLongitude(dto.getPickupLng())
                .dropoffLatitude(dto.getDropoffLat())
                .dropoffLongitude(dto.getDropoffLng())
                .estimatedFare(dto.getEstimatedFare())
                .surgeMultiplier(1.0)
                .status(RideStatus.REQUESTED)
                .build();

        Ride savedRide = rideRepository.save(ride);

        // Publish RIDE_REQUESTED Kafka Event for the Matching Engine to pick up
        rideEventProducer.publishRideEvent("RIDE_REQUESTED", savedRide.getId(), savedRide.getRiderId(), null, savedRide);
        return savedRide;
    }

    @Transactional
    public Ride assignDriver(String rideId, AssignDriverDto dto) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found: " + rideId));

        ride.setDriverId(dto.getDriverId());
        ride.setStatus(RideStatus.MATCHED);
        Ride updatedRide = rideRepository.save(ride);

        // Publish DRIVER_ASSIGNED Kafka Event
        rideEventProducer.publishRideEvent("DRIVER_ASSIGNED", updatedRide.getId(), updatedRide.getRiderId(), dto.getDriverId(), updatedRide);
        return updatedRide;
    }

    @Transactional
    public Ride completeRide(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found: " + rideId));

        ride.setStatus(RideStatus.COMPLETED);
        Ride updatedRide = rideRepository.save(ride);

        // Publish RIDE_COMPLETED Kafka Event for Payment & Analytics
        rideEventProducer.publishRideEvent("RIDE_COMPLETED", updatedRide.getId(), updatedRide.getRiderId(), updatedRide.getDriverId(), updatedRide);
        return updatedRide;
    }

    public Ride getRideById(String rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found: " + rideId));
    }
}
