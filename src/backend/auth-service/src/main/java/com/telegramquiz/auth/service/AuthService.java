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

import jakarta.persistence.EntityNotFoundException;
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
                        request.username(),
                        request.password()
                )
        );

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
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

    public String refreshAccessToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String username = jwtUtil.extractUsername(refreshToken);
        String stored = redisTemplate.opsForValue().get("refresh:" + username);
        if (stored == null || !stored.equals(refreshToken)) {
            throw new IllegalArgumentException("Refresh token not found or revoked");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }
        String role = user.getRoles().contains("ROLE_ADMIN") ? "ROLE_ADMIN" : "ROLE_MEMBER";
        return jwtUtil.generateAccessToken(username, user.getId(), role,
                user.getGoogleSub() != null ? "google" : "local");
    }
}
