package com.telegramquiz.main.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record QuestionRequestDto(
        @NotBlank String questionText,
        Integer orderIndex,
        String intro,
        Boolean introBlue,
        List<String> questionImagePaths,
        List<String> options,
        String answer,
        Boolean expectPhoto,
        Boolean isBriefing,
        String hintText,
        List<String> hintImagePaths,
        List<String> explanationTexts,
        List<String> explanationImagePaths
) {}
