package com.tienlv.be.controller;

import com.tienlv.be.dto.user.BuyTurnsResponse;
import com.tienlv.be.dto.user.ProfileResponse;
import com.tienlv.be.security.AuthenticatedUser;
import com.tienlv.be.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ProfileResponse myProfile(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return userService.getMyProfile(authenticatedUser.userId());
    }

    @PostMapping("/turns")
    public BuyTurnsResponse buyTurns(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return userService.buyTurns(authenticatedUser.userId());
    }
}
