package com.telegramquiz.main.dto;

import java.util.List;

public record ExportResponseDto(
    String version,
    String exportedAt,
    String exportedBy,
    String sourceApp,
    List<ExportedQuestionDto> questions
) {}
