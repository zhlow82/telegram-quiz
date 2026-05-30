package com.telegramquiz.auth.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.telegramquiz.auth.dto.LoginRequest;
import com.telegramquiz.auth.dto.LoginResponse;
import com.telegramquiz.auth.model.User;
import com.telegramquiz.auth.repository.UserRepository;
import com.telegramquiz.auth.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String role = user.getRoles().contains("ROLE_ADMIN") ? "ROLE_ADMIN" : "ROLE_MEMBER";
        String accessToken = jwtUtil.generateAccessToken(username, user.getId(), role, "local");
        String refreshToken = jwtUtil.generateRefreshToken(username);

        // Store refresh token in Redis with 7-day TTL
        redisTemplate.opsForValue().set(
                "refresh:" + username,
                refreshToken,
                7,
                TimeUnit.DAYS
        );

        return new LoginResponse(accessToken, refreshToken);
    }

    public void logout(String username) {
        redisTemplate.delete("refresh:" + username);
    }
}
