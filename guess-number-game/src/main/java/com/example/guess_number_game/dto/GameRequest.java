package com.example.guess_number_game.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class GameRequest {
    @Min(1)
    @Max(5)
    private int guessedNumber;

}
