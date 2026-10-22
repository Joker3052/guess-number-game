package com.example.guess_number_game.dto;

import java.io.Serializable;

public record LeaderboardResponse(String username, int score) implements Serializable {
    private static final long serialVersionUID = 1L;
}
