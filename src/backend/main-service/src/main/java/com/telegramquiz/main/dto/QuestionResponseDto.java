package com.telegramquiz.main.dto;

import java.time.LocalDateTime;
import java.util.List;

public record QuestionResponseDto(
        Long id,
        int orderIndex,
        String questionText,
        String intro,
        boolean introBlue,
        List<String> questionImagePaths,
        List<String> options,
        String answer,
        boolean expectPhoto,
        boolean isBriefing,
        String hintText,
        List<String> hintImagePaths,
        List<String> explanationTexts,
        List<String> explanationImagePaths,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
