package com.example.guess_number_game.service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.List;

import com.example.guess_number_game.dto.GameRequest;
import com.example.guess_number_game.dto.GameResult;
import com.example.guess_number_game.dto.LeaderboardResponse;
import com.example.guess_number_game.event.LeaderboardChangedEvent;
import com.example.guess_number_game.user.User;
import com.example.guess_number_game.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameService {
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 5;
    private static final int WIN_PERCENTAGE = 5;
    private static final int WIN_POINTS = 1;

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public GameService(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public GameResult guessNumber(GameRequest request, String username) {
        User user = userRepository.findByUsernameForUpdate(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getTurns() <= 0) {
            throw new IllegalArgumentException("No turns left!");
        }

        user.setTurns(user.getTurns() - 1);

        boolean won = ThreadLocalRandom.current().nextInt(1, 101) <= WIN_PERCENTAGE;
        int secretNumber;
        if (won) {
            // The 5% win roll determines the outcome; this makes the revealed
            // secret agree with the user's guess on a winning round.
            secretNumber = request.getGuessedNumber();
            user.setScore(user.getScore() + WIN_POINTS);
        } else {
            secretNumber = randomNumberDifferentFrom(request.getGuessedNumber());
        }

        userRepository.save(user);
        eventPublisher.publishEvent(new LeaderboardChangedEvent(user.getUsername(), won));
        return new GameResult(
                won,
                request.getGuessedNumber(),
                secretNumber,
                user.getTurns(),
                user.getScore()
        );
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "leaderboard", key = "'top10'")
    public List<LeaderboardResponse> getLeaderboard() {
        return new ArrayList<>(userRepository.findTop10ByOrderByScoreDesc());
    }

    private int randomNumberDifferentFrom(int guessedNumber) {
        int number = ThreadLocalRandom.current().nextInt(MIN_NUMBER, MAX_NUMBER);
        return number >= guessedNumber ? number + 1 : number;
    }
}
