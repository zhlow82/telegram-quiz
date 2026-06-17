package com.telegramquiz.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.telegramquiz.auth.dto.LoginRequest;
import com.telegramquiz.auth.dto.LoginResponse;
import com.telegramquiz.auth.repository.UserRepository;
import com.telegramquiz.auth.security.JwtUtil;
import com.telegramquiz.auth.service.AuthService;

import com.telegramquiz.auth.model.AppSetting;
import com.telegramquiz.auth.repository.AppSettingRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppSettingRepository appSettingRepository;

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String newPassword
    ) {}

    public record UpdateProfileRequest(
            @Size(max = 100) String firstName,
            @Size(max = 100) String lastName
    ) {}

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return ResponseEntity.ok(Map.of(
                "username",  username,
                "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                "lastName",  user.getLastName()  != null ? user.getLastName()  : "",
                "email",     user.getEmail()     != null ? user.getEmail()     : "",
                "provider",  user.getGoogleSub() != null ? "google" : "local"
        ));
    }

    @PatchMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody UpdateProfileRequest req) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (req.firstName() != null) user.setFirstName(req.firstName().trim().isEmpty() ? null : req.firstName().trim());
        if (req.lastName()  != null) user.setLastName(req.lastName().trim().isEmpty()   ? null : req.lastName().trim());
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody ChangePasswordRequest req) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (user.getGoogleSub() != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Password change is not available for Google accounts."));
        }
        if (!passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Current password is incorrect."));
        }
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.noContent().build();
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.noContent().build();
        }
        String username = jwtUtil.extractUsername(token);
        authService.logout(username);
        return ResponseEntity.noContent().build();
    }

    public record RefreshRequest(@NotBlank String refreshToken) {}

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody @Valid RefreshRequest req) {
        try {
            String newAccessToken = authService.refreshAccessToken(req.refreshToken());
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token invalid or expired"));
        }
    }

    @GetMapping("/settings/branding")
    public ResponseEntity<Map<String, Object>> getBranding() {
        String appName = appSettingRepository.findByKey("app_name")
                .map(AppSetting::getValue).orElse("Telegram Quiz");
        String loginWelcomeText = appSettingRepository.findByKey("login_welcome_text")
                .map(AppSetting::getValue).orElse("");
        String blobId = appSettingRepository.findByKey("app_logo_blob_id")
                .map(AppSetting::getValue).orElse(null);
        String appLogoUrl = blobId != null ? "/api/files/" + blobId : null;
        var result = new java.util.HashMap<String, Object>();
        result.put("appName", appName);
        result.put("loginWelcomeText", loginWelcomeText);
        result.put("appLogoUrl", appLogoUrl);
        return ResponseEntity.ok(result);
    }
}
