package com.example.guess_number_game.payment;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class PayPalApiClient {
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() { };

    private final RestClient restClient;
    private final PayPalProperties properties;

    private String cachedAccessToken;
    private long tokenExpiresAtMillis;

    public PayPalApiClient(RestClient.Builder builder, PayPalProperties properties) {
        this.restClient = builder.baseUrl(properties.getBaseUrl()).build();
        this.properties = properties;
    }

    public CreatedOrder createOrder(PaymentOrder paymentOrder) {
        Map<String, Object> purchaseUnit = Map.of(
                "reference_id", paymentOrder.getId().toString(),
                "custom_id", paymentOrder.getId().toString(),
                "invoice_id", "GAME-" + paymentOrder.getId(),
                "description", paymentOrder.getTurnsToGrant() + " game turns",
                "amount", Map.of(
                        "currency_code", paymentOrder.getCurrency(),
                        "value", paymentOrder.getAmount().toPlainString()
                )
        );
        Map<String, Object> paypalSource = Map.of("paypal", Map.of("experience_context", Map.of(
                "return_url", frontendUrl("/?payment=success"),
                "cancel_url", frontendUrl("/?payment=cancel"),
                "user_action", "PAY_NOW",
                "shipping_preference", "NO_SHIPPING"
        )));
        Map<String, Object> payload = Map.of(
                "intent", "CAPTURE",
                "purchase_units", List.of(purchaseUnit),
                "payment_source", paypalSource
        );

        Map<String, Object> response = restClient.post()
                .uri("/v2/checkout/orders")
                .headers(headers -> {
                    headers.setBearerAuth(accessToken());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set("PayPal-Request-Id", "game-payment-" + paymentOrder.getId() + "-create");
                })
                .body(payload)
                .retrieve()
                .body(MAP_TYPE);

        if (response == null || !(response.get("id") instanceof String paypalOrderId)) {
            throw new IllegalStateException("PayPal did not return an order ID");
        }
        String approvalUrl = null;
        if (response.get("links") instanceof List<?> links) {
            for (Object linkValue : links) {
                Map<String, Object> link = asMap(linkValue);
                if ("payer-action".equals(link.get("rel")) || "approve".equals(link.get("rel"))) {
                    approvalUrl = (String) link.get("href");
                    break;
                }
            }
        }
        if (approvalUrl == null || approvalUrl.isBlank()) {
            throw new IllegalStateException("PayPal did not return an approval link");
        }
        return new CreatedOrder(paypalOrderId, approvalUrl);
    }

    public CapturedPayment captureOrder(PaymentOrder paymentOrder) {
        Map<String, Object> response;
        try {
            response = restClient.post()
                    .uri("/v2/checkout/orders/{orderId}/capture", paymentOrder.getPaypalOrderId())
                    .headers(headers -> {
                        headers.setBearerAuth(accessToken());
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.set("PayPal-Request-Id", "game-payment-" + paymentOrder.getId() + "-capture");
                    })
                    .retrieve()
                    .body(MAP_TYPE);
        } catch (RestClientResponseException exception) {
            // Recover if a previous capture succeeded but the application lost its response.
            if (exception.getStatusCode().value() != 422) throw exception;
            response = restClient.get()
                    .uri("/v2/checkout/orders/{orderId}", paymentOrder.getPaypalOrderId())
                    .headers(headers -> headers.setBearerAuth(accessToken()))
                    .retrieve()
                    .body(MAP_TYPE);
        }

        if (response == null || !"COMPLETED".equals(response.get("status"))) {
            throw new IllegalStateException("PayPal payment is not completed");
        }

        Map<String, Object> purchaseUnit = firstMap(response.get("purchase_units"));
        Map<String, Object> payments = asMap(purchaseUnit.get("payments"));
        Map<String, Object> capture = firstMap(payments.get("captures"));
        Map<String, Object> amount = asMap(capture.get("amount"));
        String captureId = (String) capture.get("id");
        String status = (String) capture.get("status");
        String currency = (String) amount.get("currency_code");
        String value = (String) amount.get("value");
        String customId = (String) capture.getOrDefault("custom_id", purchaseUnit.get("custom_id"));

        if (!"COMPLETED".equals(status) || captureId == null || value == null || currency == null) {
            throw new IllegalStateException("PayPal did not return a completed capture");
        }
        return new CapturedPayment(captureId, value, currency, customId);
    }

    private synchronized String accessToken() {
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpiresAtMillis) {
            return cachedAccessToken;
        }
        if (properties.getClientId().isBlank() || properties.getClientSecret().isBlank()) {
            throw new IllegalStateException("Configure PAYPAL_CLIENT_ID and PAYPAL_CLIENT_SECRET first");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        Map<String, Object> tokenResponse = restClient.post()
                .uri("/v1/oauth2/token")
                .headers(headers -> {
                    headers.setBasicAuth(properties.getClientId(), properties.getClientSecret());
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .body(form)
                .retrieve()
                .body(MAP_TYPE);

        if (tokenResponse == null || !(tokenResponse.get("access_token") instanceof String token)) {
            throw new IllegalStateException("PayPal did not return an access token");
        }
        long expiresIn = tokenResponse.get("expires_in") instanceof Number seconds ? seconds.longValue() : 300;
        cachedAccessToken = token;
        tokenExpiresAtMillis = System.currentTimeMillis() + Math.max(30, expiresIn - 60) * 1000;
        return cachedAccessToken;
    }

    private String frontendUrl(String path) {
        return properties.getFrontendBaseUrl().replaceAll("/+$", "") + path;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private static Map<String, Object> firstMap(Object value) {
        return value instanceof List<?> list && !list.isEmpty() ? asMap(list.getFirst()) : Map.of();
    }

    public record CapturedPayment(String captureId, String amount, String currency, String customId) { }
    public record CreatedOrder(String paypalOrderId, String approvalUrl) { }
}
