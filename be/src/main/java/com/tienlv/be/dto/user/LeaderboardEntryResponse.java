package com.tienlv.be.dto.user;

public record LeaderboardEntryResponse(
        String username,
        int score
) {
}
