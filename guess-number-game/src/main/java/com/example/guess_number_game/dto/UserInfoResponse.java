package com.example.guess_number_game.dto;

import java.io.Serializable;

public record UserInfoResponse(Long id, String username, String email, int score, int turns)
        implements Serializable {
    private static final long serialVersionUID = 1L;
}
