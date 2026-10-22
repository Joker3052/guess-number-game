package com.example.guess_number_game.dto;

public record LoginResponse(Long userId, String username, String tokenType, String accessToken) {
}
