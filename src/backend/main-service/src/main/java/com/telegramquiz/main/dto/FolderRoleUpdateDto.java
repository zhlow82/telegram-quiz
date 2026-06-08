package com.telegramquiz.main.dto;

import jakarta.validation.constraints.NotBlank;

public record FolderRoleUpdateDto(@NotBlank String role) {}
