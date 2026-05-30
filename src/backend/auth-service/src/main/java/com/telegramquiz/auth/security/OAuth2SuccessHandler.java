package com.telegramquiz.auth.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.telegramquiz.auth.model.User;
import com.telegramquiz.auth.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final String FRONTEND_BASE = "http://localhost:5173/tg-quiz";
    private static final String REDIS_PREFIX = "oauth2:pending:";

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redis;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String sub = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");

        Optional<User> existing = userRepository.findByGoogleSub(sub);

        if (existing.isPresent()) {
            // Known user — issue JWT and send to app
            User user = existing.get();
            String role = user.getRoles().contains("ROLE_ADMIN") ? "ROLE_ADMIN" : "ROLE_MEMBER";
            String token = jwtUtil.generateAccessToken(user.getUsername(), user.getId(), role, "google");
            response.sendRedirect(FRONTEND_BASE + "/oauth2/callback?token=" + token);
        } else {
            // Unknown user — store Google info in Redis for 10 minutes, redirect to registration page
            String state = UUID.randomUUID().toString();
            redis.opsForHash().putAll(REDIS_PREFIX + state, Map.of(
                    "sub", sub,
                    "email",     email     != null ? email     : "",
                    "firstName", firstName != null ? firstName : "",
                    "lastName",  lastName  != null ? lastName  : ""
            ));
            redis.expire(REDIS_PREFIX + state, Duration.ofMinutes(10));
            response.sendRedirect(FRONTEND_BASE + "/oauth2/register?state=" + state);
        }
    }
}
