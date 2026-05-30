package com.telegramquiz.main.dto;

import java.time.LocalDateTime;
import java.util.List;

public record QuizResponseDto(
        Long id,
        String name,
        String botTokenMasked,
        String botUsername,
        int timePerQuestionSeconds,
        int passScorePercent,
        String status,
        LocalDateTime createdAt,
        List<QuestionResponseDto> questions
) {}
