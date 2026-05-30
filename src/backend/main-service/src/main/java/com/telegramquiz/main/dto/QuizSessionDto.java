package com.telegramquiz.main.dto;

import java.time.LocalDateTime;

public record QuizSessionDto(
        Long id,
        Long telegramUserId,
        String telegramUsername,
        String telegramFirstName,
        int score,
        int totalQuestions,
        boolean passed,
        String status,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {}
