package com.telegramquiz.main.dto;

public record ValidateTokenResponseDto(boolean valid, String botName, String username) {}
