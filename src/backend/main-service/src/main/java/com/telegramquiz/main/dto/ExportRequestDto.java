package com.telegramquiz.main.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record ExportRequestDto(
    @NotEmpty List<Long> questionIds,
    boolean includeImages
) {}
