package com.tienlv.be.controller;

import com.tienlv.be.dto.game.GuessRequest;
import com.tienlv.be.dto.game.GuessResponse;
import com.tienlv.be.security.AuthenticatedUser;
import com.tienlv.be.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/guesses")
    public GuessResponse guess(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                               @Valid @RequestBody GuessRequest request) {
        return gameService.guess(authenticatedUser.userId(), request.guess());
    }
}
