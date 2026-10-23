package com.example.guess_number_game.controller;

import com.example.guess_number_game.payment.CapturePayPalOrderRequest;
import com.example.guess_number_game.payment.CreatePayPalOrderRequest;
import com.example.guess_number_game.payment.CreatePayPalOrderResponse;
import com.example.guess_number_game.payment.PayPalPaymentService;
import com.example.guess_number_game.payment.PayPalPackageResponse;
import com.example.guess_number_game.payment.PaymentResult;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/paypal")
public class PayPalPaymentController {
    private final PayPalPaymentService paymentService;

    public PayPalPaymentController(PayPalPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/packages")
    public ResponseEntity<PayPalPackageResponse> getPackage() {
        return ResponseEntity.ok(paymentService.getTurnsPackage());
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(
            @AuthenticationPrincipal String username,
            @RequestBody CreatePayPalOrderRequest request
    ) {
        try {
            return ResponseEntity.ok(paymentService.createOrder(username, request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", String.valueOf(exception.getMessage())));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", String.valueOf(exception.getMessage())));
        }
    }

    @PostMapping("/capture")
    public ResponseEntity<?> captureOrder(
            @AuthenticationPrincipal String username,
            @RequestBody CapturePayPalOrderRequest request
    ) {
        try {
            return ResponseEntity.ok(paymentService.captureOrder(username, request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", String.valueOf(exception.getMessage())));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", String.valueOf(exception.getMessage())));
        }
    }
}
