package com.example.guess_number_game.payment;

import java.math.BigDecimal;

public record CreatePayPalOrderResponse(
        Long paymentId,
        String paypalOrderId,
        String approvalUrl,
        int turns,
        BigDecimal amount,
        String currency
) {
}
