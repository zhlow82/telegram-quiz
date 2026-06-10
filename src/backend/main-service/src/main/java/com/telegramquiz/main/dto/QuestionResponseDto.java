package com.telegramquiz.main.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.telegramquiz.main.model.ContentBlock;

public record QuestionResponseDto(
        Long id,
        String createdBy,
        int orderIndex,
        List<ContentBlock> questionBlocks,
        List<String> options,
        String answer,
        boolean expectPhoto,
        boolean isBriefing,
        boolean expectsTextInput,
        String briefingPrimaryButtonText,
        boolean showBriefingPrimaryButton,
        String briefingSecondaryButtonText,
        boolean showBriefingSecondaryButton,
        String afterAnswerButtonText,
        boolean showAfterAnswerButton,
        List<ContentBlock> hintBlocks,
        List<ContentBlock> explanationBlocks,
        Integer mark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String updatedBy,
        Long folderId
) {}
