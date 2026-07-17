package com.tienlv.be.service;

import com.tienlv.be.dto.user.LeaderboardEntryResponse;
import com.tienlv.be.entity.User;
import com.tienlv.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Cacheable("leaderboard")
    public List<LeaderboardEntryResponse> getLeaderboard() {
        List<User> users = userRepository.findTop10ByOrderByScoreDescIdAsc();
        return users.stream()
                .map(user -> new LeaderboardEntryResponse(user.getUsername(), user.getScore()))
                .toList();
    }
}
