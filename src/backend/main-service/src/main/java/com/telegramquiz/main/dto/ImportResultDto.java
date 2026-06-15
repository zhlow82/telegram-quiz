package com.telegramquiz.main.dto;

import java.util.List;

public record ImportResultDto(
    int imported,
    List<String> errors
) {}
