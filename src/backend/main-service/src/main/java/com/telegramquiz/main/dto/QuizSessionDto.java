package com.telegramquiz.main.dto;

import java.time.LocalDateTime;

public record QuizSessionDto(
        Long id,
        Long telegramUserId,
        String telegramUsername,
        String telegramFirstName,
        String teamName,
        int score,
        int totalQuestions,
        boolean passed,
        String status,
        int currentQuestionIndex,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime lastActivityAt,
        LocalDateTime abandonedAt
) {}
