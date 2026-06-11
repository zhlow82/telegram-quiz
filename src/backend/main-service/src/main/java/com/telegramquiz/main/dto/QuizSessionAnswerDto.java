package com.telegramquiz.main.dto;

import java.time.LocalDateTime;

public record QuizSessionAnswerDto(
        Long id,
        Long sessionId,
        Long questionId,
        String selectedAnswer,
        Boolean isCorrect,
        String photoFileId,
        String photoCaption,
        Integer responseTimeMs,
        LocalDateTime answeredAt
) {}
