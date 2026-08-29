package com.ridepulse.booking.repository;

import com.ridepulse.booking.entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, String> {
    List<Ride> findByRiderId(String riderId);
    List<Ride> findByDriverId(String driverId);
}
