package com.telegramquiz.main.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuizRequestDto(
        @NotBlank String name,
        @NotBlank String botToken,
        String botUsername,
        @Min(5) @Max(300) int timePerQuestionSeconds,
        @Min(0) @Max(100) int passScorePercent,
        @NotNull List<Long> questionIds
) {}
