package com.example.guess_number_game.payment;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    Optional<PaymentOrder> findByPaypalOrderIdAndUsername(String paypalOrderId, String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentOrder p where p.paypalOrderId = :paypalOrderId and p.username = :username")
    Optional<PaymentOrder> findByPaypalOrderIdAndUsernameForUpdate(
            @Param("paypalOrderId") String paypalOrderId,
            @Param("username") String username
    );
}
