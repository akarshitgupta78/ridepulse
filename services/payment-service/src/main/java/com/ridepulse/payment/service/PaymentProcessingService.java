package com.ridepulse.payment.service;

import com.ridepulse.payment.entity.Payment;
import com.ridepulse.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessingService {

    private final PaymentRepository paymentRepository;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String IDEMPOTENCY_KEY_PREFIX = "payment:processed:ride:";

    @Transactional
    public void processPaymentForCompletedRide(String rideId, String riderId, String driverId, BigDecimal fare) {
        String lockKey = IDEMPOTENCY_KEY_PREFIX + rideId;

        // 1. Atomic Redis SETNX to ensure idempotency across concurrent duplicate events
        Boolean isNewEvent = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "PROCESSED", Duration.ofHours(24));

        if (Boolean.FALSE.equals(isNewEvent)) {
            log.warn("Duplicate payment event received for rideId: {}. Skipping execution to prevent double charge.", rideId);
            return;
        }

        // 2. Double-check database record
        if (paymentRepository.findByRideId(rideId).isPresent()) {
            log.warn("Payment record already exists in DB for rideId: {}", rideId);
            return;
        }

        log.info("Processing fare settlement: Ride {} | Rider {} | Amount: ${}", rideId, riderId, fare);

        // 3. Mock Payment Gateway Settlement (Stripe/Razorpay placeholder)
        String txnRef = "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = Payment.builder()
                .rideId(rideId)
                .riderId(riderId)
                .driverId(driverId)
                .amount(fare != null ? fare : new BigDecimal("25.00"))
                .status("SUCCESS")
                .transactionRef(txnRef)
                .build();

        paymentRepository.save(payment);
        log.info("Payment settled successfully for ride {}. Transaction Reference: {}", rideId, txnRef);
    }
}
