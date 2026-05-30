package com.telegramquiz.main.bot;

import java.util.List;

import com.telegramquiz.main.model.ContentBlock;

public record QuizBotQuestion(
        List<ContentBlock> questionBlocks,
        List<String> options,
        String answer,
        boolean isBriefing,
        List<ContentBlock> hintBlocks,
        List<ContentBlock> explanationBlocks
) {}
