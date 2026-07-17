package com.tienlv.be.dto.user;

public record BuyTurnsResponse(
        String message,
        int remainingTurns
) {
}
