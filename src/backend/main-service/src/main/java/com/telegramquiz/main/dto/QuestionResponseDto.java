package com.telegramquiz.main.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.telegramquiz.main.model.ContentBlock;

public record QuestionResponseDto(
        Long id,
        int orderIndex,
        List<ContentBlock> questionBlocks,
        List<String> options,
        String answer,
        boolean expectPhoto,
        boolean isBriefing,
        List<ContentBlock> hintBlocks,
        List<ContentBlock> explanationBlocks,
        Integer mark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
