package com.example.guess_number_game.controller;

import com.example.guess_number_game.dto.GameRequest;
import com.example.guess_number_game.dto.GameResult;
import com.example.guess_number_game.dto.LeaderboardResponse;
import com.example.guess_number_game.dto.UserInfoResponse;
import com.example.guess_number_game.service.GameService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game")
public class GameController {
    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/guess")
    public ResponseEntity<?> guessNumber(
            @Valid @RequestBody GameRequest request,
            @AuthenticationPrincipal String username
    ) {
        try {
            return ResponseEntity.ok(gameService.guessNumber(request, username));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @PostMapping("/buy-turns")
    public ResponseEntity<UserInfoResponse> buyTurns(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok(gameService.buyTurns(username));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<java.util.List<LeaderboardResponse>> getLeaderboard() {
        return ResponseEntity.ok(gameService.getLeaderboard());
    }
}
