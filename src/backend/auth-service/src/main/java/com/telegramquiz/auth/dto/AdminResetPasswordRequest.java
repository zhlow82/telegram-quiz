package com.telegramquiz.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminResetPasswordRequest(
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String newPassword) {}
