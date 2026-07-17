package com.tienlv.be.service;

import com.tienlv.be.dto.user.BuyTurnsResponse;
import com.tienlv.be.dto.user.ProfileResponse;
import com.tienlv.be.entity.User;
import com.tienlv.be.exception.NotFoundException;
import com.tienlv.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return new ProfileResponse(user.getEmail(), user.getScore(), user.getTurns());
    }

    @Transactional
    public BuyTurnsResponse buyTurns(Long userId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setTurns(user.getTurns() + 5);
        userRepository.save(user);
        return new BuyTurnsResponse("Mua lượt chơi thành công", user.getTurns());
    }
}
