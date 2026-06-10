package com.telegramquiz.auth.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.telegramquiz.auth.dto.AdminResetPasswordRequest;
import com.telegramquiz.auth.dto.AdminUpdateProfileRequest;
import com.telegramquiz.auth.dto.CreateUserRequest;
import com.telegramquiz.auth.dto.GoogleSettingsRequest;
import com.telegramquiz.auth.dto.InvitationCodeResponse;
import com.telegramquiz.auth.dto.UpdateRoleRequest;
import com.telegramquiz.auth.dto.UserResponse;
import com.telegramquiz.auth.model.AppSetting;
import com.telegramquiz.auth.repository.AppSettingRepository;
import com.telegramquiz.auth.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AppSettingRepository appSettingRepository;

    @GetMapping("/invitation-codes")
    public ResponseEntity<List<InvitationCodeResponse>> listCodes() {
        return ResponseEntity.ok(adminService.listCodes());
    }

    @PostMapping("/invitation-codes")
    public ResponseEntity<InvitationCodeResponse> generateCode(@AuthenticationPrincipal String adminUsername) {
        return ResponseEntity.ok(adminService.generateCode(adminUsername));
    }

    @DeleteMapping("/invitation-codes/{id}")
    public ResponseEntity<Void> deactivateCode(@PathVariable Long id) {
        adminService.deactivateCode(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/invitation-codes/{id}/permanent")
    public ResponseEntity<Void> deleteCode(@PathVariable Long id) {
        adminService.deleteCode(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/invitation-codes/{id}/activate")
    public ResponseEntity<Void> activateCode(@PathVariable Long id) {
        adminService.activateCode(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        adminService.setUserActive(id, true);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        adminService.setUserActive(id, false);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, @AuthenticationPrincipal String adminUsername) {
        adminService.deleteUser(id, adminUsername);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(adminService.createUser(request));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(adminService.updateRole(id, request));
    }

    @PatchMapping("/users/{id}/profile")
    public ResponseEntity<UserResponse> updateProfile(@PathVariable Long id,
                                                      @RequestBody AdminUpdateProfileRequest request) {
        return ResponseEntity.ok(adminService.updateProfile(id, request));
    }

    @PatchMapping("/users/{id}/password")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id,
                                              @Valid @RequestBody AdminResetPasswordRequest request) {
        adminService.resetPassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/settings/google")
    public ResponseEntity<Map<String, Object>> getGoogleSettings() {
        String clientId = appSettingRepository.findByKey("google_client_id")
                .map(AppSetting::getValue).orElse("");
        boolean secretConfigured = appSettingRepository.findByKey("google_client_secret").isPresent();
        return ResponseEntity.ok(Map.of(
                "clientId", clientId,
                "secretConfigured", secretConfigured
        ));
    }

    @PutMapping("/settings/google")
    public ResponseEntity<Void> saveGoogleSettings(@Valid @RequestBody GoogleSettingsRequest request) {
        appSettingRepository.save(new AppSetting("google_client_id", request.clientId()));
        if (request.clientSecret() != null && !request.clientSecret().isBlank()) {
            appSettingRepository.save(new AppSetting("google_client_secret", request.clientSecret()));
        }
        return ResponseEntity.noContent().build();
    }

    public record BrandingSettingsRequest(String appName, String loginWelcomeText, String appLogoBlobId) {}

    @PutMapping("/settings/branding")
    public ResponseEntity<Void> saveBrandingSettings(@RequestBody BrandingSettingsRequest request) {
        if (request.appName() != null) {
            String name = request.appName().trim().isEmpty() ? "Telegram Quiz" : request.appName().trim();
            appSettingRepository.save(new AppSetting("app_name", name));
        }
        if (request.loginWelcomeText() != null) {
            appSettingRepository.save(new AppSetting("login_welcome_text", request.loginWelcomeText().trim()));
        }
        if (request.appLogoBlobId() != null) {
            if (request.appLogoBlobId().isBlank()) {
                appSettingRepository.deleteById("app_logo_blob_id");
            } else {
                appSettingRepository.save(new AppSetting("app_logo_blob_id", request.appLogoBlobId().trim()));
            }
        }
        return ResponseEntity.noContent().build();
    }
}

