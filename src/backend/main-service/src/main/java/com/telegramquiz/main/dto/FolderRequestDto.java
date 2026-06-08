package com.telegramquiz.main.dto;

import jakarta.validation.constraints.NotBlank;

public record FolderRequestDto(@NotBlank String name) {}
