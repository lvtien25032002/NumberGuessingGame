package com.tienlv.be.service;

import com.tienlv.be.dto.game.GuessResponse;
import com.tienlv.be.entity.User;
import com.tienlv.be.exception.ForbiddenException;
import com.tienlv.be.exception.NotFoundException;
import com.tienlv.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final UserRepository userRepository;

    @Value("${game.win-rate:-1}")
    private int winRate;

    @Transactional
    @CacheEvict(value = "leaderboard", allEntries = true)
    public GuessResponse guess(Long userId, int guessedNumber) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getTurns() <= 0) {
            throw new ForbiddenException("Hết lượt chơi");
        }

        boolean isCorrect;
        int serverResult;
        user.setTurns(user.getTurns() - 1);
        if (winRate >= 0) {
            isCorrect = ThreadLocalRandom.current().nextInt(100) < winRate;
            if (isCorrect) {
                serverResult = guessedNumber;
            } else {
                do {
                    serverResult = ThreadLocalRandom.current().nextInt(1, 6);
                } while (serverResult == guessedNumber);
            }
        } else {
            serverResult = ThreadLocalRandom.current().nextInt(1, 6);
            isCorrect = guessedNumber == serverResult;
        }
        if (isCorrect) {
            user.setScore(user.getScore() + 1);
        }
        userRepository.save(user);

        return new GuessResponse(serverResult, isCorrect, user.getScore(), user.getTurns());
    }
}
