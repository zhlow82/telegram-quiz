package com.telegramquiz.main.dto;

import java.time.LocalDateTime;

public record FolderResponseDto(Long id, String name, String createdBy, LocalDateTime createdAt, String role) {}
