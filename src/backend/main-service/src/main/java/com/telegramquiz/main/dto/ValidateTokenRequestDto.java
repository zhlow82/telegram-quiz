package com.telegramquiz.main.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateTokenRequestDto(@NotBlank String token) {}
