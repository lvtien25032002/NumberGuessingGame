package com.tienlv.be.dto.user;

public record ProfileResponse(
        String email,
        int score,
        int turns
) {
}
