package com.example.guess_number_game.service;

import com.example.guess_number_game.dto.LoginRequest;
import com.example.guess_number_game.dto.LoginResponse;
import com.example.guess_number_game.dto.UserInfoResponse;
import com.example.guess_number_game.dto.RegisterRequest;
import com.example.guess_number_game.config.JwtService;
import com.example.guess_number_game.event.LeaderboardChangedEvent;
import com.example.guess_number_game.user.User;
import com.example.guess_number_game.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getUsername(), request.getEmail(), passwordHash);
        user.setScore(0);
        user.setTurns(0);
        userRepository.save(user);
        eventPublisher.publishEvent(new LeaderboardChangedEvent(user.getUsername(), true));
        return "Registration successful";
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return new LoginResponse(user.getId(), user.getUsername(), "Bearer", jwtService.generateToken(user));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "userInfo", key = "#username")
    public UserInfoResponse getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toUserInfo(user);
    }

    private UserInfoResponse toUserInfo(User user) {
        return new UserInfoResponse(
                user.getId(), user.getUsername(), user.getEmail(), user.getScore(), user.getTurns()
        );
    }
}
