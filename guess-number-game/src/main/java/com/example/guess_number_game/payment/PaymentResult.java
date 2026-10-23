package com.example.guess_number_game.payment;

import java.math.BigDecimal;

public record PaymentResult(
        String status,
        int turnsAdded,
        int remainingTurns,
        BigDecimal amount,
        String currency
) {
}
