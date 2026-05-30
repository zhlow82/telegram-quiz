package com.telegramquiz.main.dto;

import java.util.List;

import com.telegramquiz.main.model.ContentBlock;

import jakarta.validation.constraints.NotNull;

public record QuestionRequestDto(
        @NotNull List<ContentBlock> questionBlocks,
        Integer orderIndex,
        List<String> options,
        String answer,
        Boolean expectPhoto,
        Boolean isBriefing,
        List<ContentBlock> hintBlocks,
        List<ContentBlock> explanationBlocks,
        Integer mark
) {}
