package com.tienlv.be.controller;

import com.tienlv.be.dto.user.LeaderboardEntryResponse;
import com.tienlv.be.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaderboards")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public List<LeaderboardEntryResponse> getLeaderboard() {
        return leaderboardService.getLeaderboard();
    }
}
