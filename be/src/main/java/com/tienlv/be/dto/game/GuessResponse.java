package com.tienlv.be.dto.game;

public record GuessResponse(
        int serverResult,
        boolean isCorrect,
        int currentScore,
        int remainingTurns
) {
}
