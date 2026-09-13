package com.ridepulse.driver.repository;

import com.ridepulse.driver.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, String> {
    Optional<Driver> findByPhone(String phone);
}
