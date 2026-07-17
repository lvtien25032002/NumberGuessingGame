package com.tienlv.be.service;

import com.tienlv.be.dto.game.GuessResponse;
import com.tienlv.be.entity.User;
import com.tienlv.be.exception.ForbiddenException;
import com.tienlv.be.exception.NotFoundException;
import com.tienlv.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class GameService {

    private final UserRepository userRepository;

    @Transactional
    @CacheEvict(value = "leaderboard", allEntries = true)
    public GuessResponse guess(Long userId, int guessedNumber) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getTurns() <= 0) {
            throw new ForbiddenException("Hết lượt chơi");
        }

        int serverResult = ThreadLocalRandom.current().nextInt(1, 6);
        boolean isCorrect = guessedNumber == serverResult;

        user.setTurns(user.getTurns() - 1);
        if (isCorrect) {
            user.setScore(user.getScore() + 1);
        }
        userRepository.save(user);

        return new GuessResponse(serverResult, isCorrect, user.getScore(), user.getTurns());
    }
}
