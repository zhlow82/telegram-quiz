package com.telegramquiz.main.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReorderRequestDto(@NotEmpty List<Long> orderedIds) {}
