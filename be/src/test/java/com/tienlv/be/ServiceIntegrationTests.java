package com.tienlv.be;

import com.tienlv.be.dto.auth.LoginRequest;
import com.tienlv.be.dto.auth.LoginResponse;
import com.tienlv.be.dto.auth.RegisterRequest;
import com.tienlv.be.dto.game.GuessResponse;
import com.tienlv.be.dto.user.BuyTurnsResponse;
import com.tienlv.be.dto.user.LeaderboardEntryResponse;
import com.tienlv.be.dto.user.ProfileResponse;
import com.tienlv.be.entity.User;
import com.tienlv.be.repository.UserRepository;
import com.tienlv.be.service.AuthService;
import com.tienlv.be.service.GameService;
import com.tienlv.be.service.LeaderboardService;
import com.tienlv.be.service.UserService;

import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RequiredArgsConstructor
@org.springframework.test.context.TestConstructor(autowireMode = org.springframework.test.context.TestConstructor.AutowireMode.ALL)
class ServiceIntegrationTests {

    private final AuthService authService;

    private final GameService gameService;

    private final UserService userService;

    private final LeaderboardService leaderboardService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerLoginAndProfileFlow() {
        authService.register(new RegisterRequest("player1", "player1@example.com", "password123"));
        LoginResponse loginResponse = authService.login(new LoginRequest("player1", "password123"));
        assertThat(loginResponse.token()).isNotBlank();
        assertThat(loginResponse.tokenType()).isEqualTo("Bearer");

        User user = userRepository.findByUsername("player1").orElseThrow();
        ProfileResponse profile = userService.getMyProfile(user.getId());
        assertThat(profile.email()).isEqualTo("player1@example.com");
        assertThat(profile.score()).isEqualTo(0);
        assertThat(profile.turns()).isEqualTo(5);
    }

    @Test
    void guessConsumesTurnAndBuyTurnsWorks() {
        authService.register(new RegisterRequest("player2", "player2@example.com", "password123"));
        User user = userRepository.findByUsername("player2").orElseThrow();

        GuessResponse guessResponse = gameService.guess(user.getId(), 3);
        assertThat(guessResponse.remainingTurns()).isEqualTo(4);

        BuyTurnsResponse buyTurnsResponse = userService.buyTurns(user.getId());
        assertThat(buyTurnsResponse.remainingTurns()).isEqualTo(9);
    }

    @Test
    void leaderboardReturnsTopTenSortedByScoreDescThenIdAsc() {
        for (int i = 0; i < 12; i++) {
            User user = new User();
            user.setUsername("u" + i);
            user.setEmail("u" + i + "@example.com");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setScore(12 - i);
            user.setTurns(5);
            userRepository.save(user);
        }

        List<LeaderboardEntryResponse> leaderboard = leaderboardService.getLeaderboard();
        assertThat(leaderboard).hasSize(10);
        assertThat(leaderboard.get(0).username()).isEqualTo("u0");
        assertThat(leaderboard.get(9).username()).isEqualTo("u9");
    }
}
