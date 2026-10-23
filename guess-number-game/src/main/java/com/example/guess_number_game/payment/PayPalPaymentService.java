package com.example.guess_number_game.payment;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

@Service
public class PayPalPaymentService {
    private static final String TURNS_PACKAGE_ID = "turns-5";

    private final PayPalProperties properties;
    private final PayPalApiClient payPalApiClient;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentFulfillmentService fulfillmentService;

    public PayPalPaymentService(
            PayPalProperties properties,
            PayPalApiClient payPalApiClient,
            PaymentOrderRepository paymentOrderRepository,
            PaymentFulfillmentService fulfillmentService
    ) {
        this.properties = properties;
        this.payPalApiClient = payPalApiClient;
        this.paymentOrderRepository = paymentOrderRepository;
        this.fulfillmentService = fulfillmentService;
    }

    public CreatePayPalOrderResponse createOrder(String username, CreatePayPalOrderRequest request) {
        requirePackage(request);
        validateConfiguration();

        BigDecimal amount = properties.getTurnsPackagePrice();
        String currency = properties.getCurrency().toUpperCase();
        int turns = properties.getTurnsPackageAmount();
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2 || turns <= 0
                || !currency.matches("[A-Z]{3}")) {
            throw new IllegalStateException("PayPal package configuration is invalid");
        }

        PaymentOrder order = paymentOrderRepository.saveAndFlush(
                new PaymentOrder(username, amount, currency, turns)
        );
        try {
            PayPalApiClient.CreatedOrder createdOrder = payPalApiClient.createOrder(order);
            order.setPaypalOrderId(createdOrder.paypalOrderId());
            order.setStatus(PaymentStatus.PENDING);
            paymentOrderRepository.save(order);
            return new CreatePayPalOrderResponse(
                    order.getId(), createdOrder.paypalOrderId(), createdOrder.approvalUrl(), turns, amount, currency
            );
        } catch (RestClientException | IllegalStateException exception) {
            order.setStatus(PaymentStatus.FAILED);
            paymentOrderRepository.save(order);
            throw new IllegalStateException("Could not create PayPal order: " + safeMessage(exception), exception);
        }
    }

    public PayPalPackageResponse getTurnsPackage() {
        return new PayPalPackageResponse(
                TURNS_PACKAGE_ID,
                properties.getTurnsPackageAmount(),
                properties.getTurnsPackagePrice(),
                properties.getCurrency().toUpperCase()
        );
    }

    public PaymentResult captureOrder(String username, CapturePayPalOrderRequest request) {
        if (request == null || request.paypalOrderId() == null || request.paypalOrderId().isBlank()) {
            throw new IllegalArgumentException("PayPal order ID is required");
        }

        PaymentOrder order = paymentOrderRepository.findByPaypalOrderIdAndUsername(
                        request.paypalOrderId(), username)
                .orElseThrow(() -> new IllegalArgumentException("Payment order not found"));
        if (order.getStatus() == PaymentStatus.PAID) {
            return fulfillmentService.getResult(request.paypalOrderId(), username);
        }
        if (order.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Payment order is not pending");
        }

        try {
            PayPalApiClient.CapturedPayment capture = payPalApiClient.captureOrder(order);
            if (!order.getId().toString().equals(capture.customId())
                    || !order.getCurrency().equals(capture.currency())
                    || new BigDecimal(capture.amount()).compareTo(order.getAmount()) != 0) {
                throw new IllegalStateException("Captured amount or payment reference does not match the order");
            }
            return fulfillmentService.completePayment(request.paypalOrderId(), username, capture);
        } catch (RestClientException exception) {
            throw new IllegalStateException("Could not confirm payment with PayPal: " + safeMessage(exception), exception);
        }
    }

    private void requirePackage(CreatePayPalOrderRequest request) {
        if (request == null || !TURNS_PACKAGE_ID.equals(request.packageId())) {
            throw new IllegalArgumentException("Unknown turns package");
        }
    }

    private void validateConfiguration() {
        if (properties.getClientId().isBlank() || properties.getClientSecret().isBlank()) {
            throw new IllegalStateException("Configure PAYPAL_CLIENT_ID and PAYPAL_CLIENT_SECRET first");
        }
    }

    private static String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null ? "PayPal request failed" : message;
    }
}
