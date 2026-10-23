package com.example.guess_number_game.payment;

import java.math.BigDecimal;

public record PayPalPackageResponse(String packageId, int turns, BigDecimal amount, String currency) {
}
