package com.tienlv.be.service;

import com.tienlv.be.dto.auth.LoginRequest;
import com.tienlv.be.dto.auth.LoginResponse;
import com.tienlv.be.dto.auth.RegisterRequest;
import com.tienlv.be.dto.common.MessageResponse;
import com.tienlv.be.entity.User;
import com.tienlv.be.exception.ConflictException;
import com.tienlv.be.exception.UnauthorizedException;
import com.tienlv.be.repository.UserRepository;
import com.tienlv.be.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setScore(0);
        user.setTurns(5);
        userRepository.save(user);

        return new MessageResponse("Đăng ký thành công");
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UnauthorizedException("Sai thông tin đăng nhập"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Sai thông tin đăng nhập");
        }

        String token = jwtService.generateToken(user.getId());
        return new LoginResponse(token, "Bearer");
    }
}
