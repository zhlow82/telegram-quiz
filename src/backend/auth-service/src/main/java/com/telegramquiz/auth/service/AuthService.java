package com.telegramquiz.auth.service;

import com.telegramquiz.auth.dto.LoginRequest;
import com.telegramquiz.auth.dto.LoginResponse;
import com.telegramquiz.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String username = authentication.getName();
        String accessToken = jwtUtil.generateAccessToken(username);
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
