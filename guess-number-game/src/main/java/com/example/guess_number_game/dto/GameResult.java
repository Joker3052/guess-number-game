package com.example.guess_number_game.dto;

public record GameResult(
        boolean won,
        int guessedNumber,
        int secretNumber,
        int remainingTurns,
        int currentScore
) {
}
