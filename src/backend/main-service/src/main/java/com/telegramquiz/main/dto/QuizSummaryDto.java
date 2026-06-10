package com.telegramquiz.main.dto;

import java.time.LocalDateTime;

public record QuizSummaryDto(
        Long id,
        String name,
        String botUsername,
        String status,
        int questionCount,
        int timePerQuestionSeconds,
        int totalTimeLimitSeconds,
        int passScorePercent,
        LocalDateTime createdAt
) {}
