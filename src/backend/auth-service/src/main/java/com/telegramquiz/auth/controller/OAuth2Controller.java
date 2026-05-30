package com.telegramquiz.auth.controller;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.telegramquiz.auth.model.InvitationCode;
import com.telegramquiz.auth.model.User;
import com.telegramquiz.auth.repository.InvitationCodeRepository;
import com.telegramquiz.auth.repository.UserRepository;
import com.telegramquiz.auth.security.DynamicClientRegistrationRepository;
import com.telegramquiz.auth.security.JwtUtil;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private static final String REDIS_PREFIX = "oauth2:pending:";

    private final StringRedisTemplate redis;
    private final UserRepository userRepository;
    private final InvitationCodeRepository invitationCodeRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final DynamicClientRegistrationRepository clientRegistrationRepository;

    public record CompleteRegistrationRequest(
            @NotBlank String state,
            @NotBlank String invitationCode
    ) {}

    public record TokenResponse(String accessToken) {}

    /** Public endpoint — frontend calls this to decide whether to show the Google sign-in button. */
    @GetMapping("/configured")
    public ResponseEntity<Map<String, Boolean>> isConfigured() {
        boolean ok = clientRegistrationRepository.isConfigured();
        return ResponseEntity.ok(Map.of("configured", ok));
    }

    /**
     * Called by the frontend after the user enters their invitation code.
     * Validates the code, creates the account, and returns a JWT.
     */
    @PostMapping("/complete")
    public ResponseEntity<?> complete(@Valid @RequestBody CompleteRegistrationRequest req) {
        // 1. Load pending Google info from Redis
        String redisKey = REDIS_PREFIX + req.state();
        Map<Object, Object> pending = redis.opsForHash().entries(redisKey);
        if (pending.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Session expired or invalid. Please sign in with Google again."));
        }

        String sub       = (String) pending.get("sub");
        String email     = (String) pending.get("email");
        String firstName = (String) pending.get("firstName");
        String lastName  = (String) pending.get("lastName");

        // 2. Check if user already registered (race condition guard)
        if (userRepository.findByGoogleSub(sub).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "This Google account is already registered."));
        }

        // 3. Validate invitation code
        Optional<InvitationCode> codeOpt = invitationCodeRepository.findByCodeAndActiveTrue(req.invitationCode().trim().toUpperCase());
        if (codeOpt.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of("message", "Invalid or inactive invitation code."));
        }

        // 4. Derive a unique username from the Google name/email
        String baseUsername = deriveUsername(firstName, email);
        String username = uniqueUsername(baseUsername);

        // 5. Create user
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // unusable random password
                .googleSub(sub)
                .email(email.isBlank() ? null : email)
                .firstName(firstName.isBlank() ? null : firstName)
                .lastName(lastName.isBlank()  ? null : lastName)
                .roles(Set.of("ROLE_MEMBER"))
                .build();
        user = userRepository.save(user);

        // 6. Clean up Redis state
        redis.delete(redisKey);

        // 7. Issue JWT
        String token = jwtUtil.generateAccessToken(username, user.getId(), "ROLE_MEMBER", "google");
        return ResponseEntity.ok(new TokenResponse(token));
    }

    private String deriveUsername(String firstName, String email) {
        if (firstName != null && !firstName.isBlank()) {
            return firstName.toLowerCase().replaceAll("[^a-z0-9]", "");
        }
        if (email != null && email.contains("@")) {
            return email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        }
        return "user";
    }

    private String uniqueUsername(String base) {
        if (base.isBlank()) base = "user";
        if (!userRepository.existsByUsername(base)) return base;
        String candidate;
        do {
            candidate = base + UUID.randomUUID().toString().substring(0, 4);
        } while (userRepository.existsByUsername(candidate));
        return candidate;
    }
}
