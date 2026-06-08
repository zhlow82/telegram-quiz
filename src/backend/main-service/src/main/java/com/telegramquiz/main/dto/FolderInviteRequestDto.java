package com.telegramquiz.main.dto;

import jakarta.validation.constraints.NotBlank;

public record FolderInviteRequestDto(
        @NotBlank String username,
        @NotBlank String role
) {}
