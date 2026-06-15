package com.telegramquiz.main.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record ImportRequestDto(
    String version,
    @NotEmpty @Valid List<ImportQuestionDto> questions
) {}
