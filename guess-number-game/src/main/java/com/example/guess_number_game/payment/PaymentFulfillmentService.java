package com.example.guess_number_game.payment;

import com.example.guess_number_game.event.LeaderboardChangedEvent;
import com.example.guess_number_game.repository.UserRepository;
import com.example.guess_number_game.user.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PaymentFulfillmentService {
    private final PaymentOrderRepository paymentOrderRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentFulfillmentService(
            PaymentOrderRepository paymentOrderRepository,
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PaymentResult completePayment(
            String paypalOrderId,
            String username,
            PayPalApiClient.CapturedPayment capture
    ) {
        PaymentOrder order = paymentOrderRepository.findByPaypalOrderIdAndUsernameForUpdate(paypalOrderId, username)
                .orElseThrow(() -> new IllegalArgumentException("Payment order not found"));

        User user = userRepository.findByUsernameForUpdate(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (order.getStatus() == PaymentStatus.PAID) {
            return toResult(order, user);
        }
        if (order.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Payment order is not pending");
        }

        order.setStatus(PaymentStatus.PAID);
        order.setPaypalCaptureId(capture.captureId());
        order.setPaidAt(Instant.now());
        user.setTurns(user.getTurns() + order.getTurnsToGrant());
        eventPublisher.publishEvent(new LeaderboardChangedEvent(user.getUsername(), false));
        return toResult(order, user);
    }

    @Transactional(readOnly = true)
    public PaymentResult getResult(String paypalOrderId, String username) {
        PaymentOrder order = paymentOrderRepository.findByPaypalOrderIdAndUsername(paypalOrderId, username)
                .orElseThrow(() -> new IllegalArgumentException("Payment order not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toResult(order, user);
    }

    private static PaymentResult toResult(PaymentOrder order, User user) {
        return new PaymentResult(
                order.getStatus().name(), order.getTurnsToGrant(), user.getTurns(), order.getAmount(), order.getCurrency()
        );
    }
}
