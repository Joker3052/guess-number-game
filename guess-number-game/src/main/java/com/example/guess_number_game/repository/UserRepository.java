package com.example.guess_number_game.repository;

import java.util.List;
import java.util.Optional;

import com.example.guess_number_game.dto.LeaderboardResponse;
import com.example.guess_number_game.user.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.username = :username")
    Optional<User> findByUsernameForUpdate(@Param("username") String username);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    List<LeaderboardResponse> findTop10ByOrderByScoreDesc();
}
