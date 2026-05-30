package com.telegramquiz.main.bot;

import java.util.List;

public record QuizBotData(
        Long quizId,
        String quizName,
        String botToken,
        String botUsername,
        int timePerQuestionSeconds,
        int passScorePercent,
        List<QuizBotQuestion> questions
) {}
