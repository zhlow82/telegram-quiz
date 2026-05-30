package com.telegramquiz.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleSettingsRequest(
        @NotBlank String clientId,
        String clientSecret  // optional — if blank, existing secret is kept
) {}
