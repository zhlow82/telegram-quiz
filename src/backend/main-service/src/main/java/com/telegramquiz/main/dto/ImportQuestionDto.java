package com.telegramquiz.main.dto;

import java.util.List;

import com.telegramquiz.main.model.ContentBlock;

public record ImportQuestionDto(
    List<ContentBlock> questionBlocks,
    List<String> options,
    String answer,
    Boolean expectPhoto,
    Boolean isBriefing,
    Boolean expectsTextInput,
    String briefingPrimaryButtonText,
    Boolean showBriefingPrimaryButton,
    String briefingSecondaryButtonText,
    Boolean showBriefingSecondaryButton,
    String afterAnswerButtonText,
    Boolean showAfterAnswerButton,
    List<ContentBlock> hintBlocks,
    List<ContentBlock> explanationBlocks,
    Integer mark
) {}
