package com.telegramquiz.main.bot;

import java.util.List;

import com.telegramquiz.main.model.ContentBlock;

public record QuizBotQuestion(
        Long id,
        List<ContentBlock> questionBlocks,
        List<String> options,
        String answer,
        boolean isBriefing,
        boolean expectPhoto,
        boolean expectsTextInput,
        String briefingPrimaryButtonText,
        boolean showBriefingPrimaryButton,
        String briefingSecondaryButtonText,
        boolean showBriefingSecondaryButton,
        String afterAnswerButtonText,
        boolean showAfterAnswerButton,
        List<ContentBlock> hintBlocks,
        List<ContentBlock> explanationBlocks
) {}
