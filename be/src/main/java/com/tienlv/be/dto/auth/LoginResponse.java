package com.tienlv.be.dto.auth;

public record LoginResponse(
        String token,
        String tokenType
) {
}
