package com.telegramquiz.main.bot;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.telegramquiz.main.entity.QuizStatus;
import com.telegramquiz.main.repository.QuizRepository;
import com.telegramquiz.main.service.BotTokenEncryptionService;
import com.telegramquiz.main.service.ImageBlobService;
import com.telegramquiz.main.service.QuizSessionService;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotManager {

    private final QuizRepository quizRepository;
    private final BotTokenEncryptionService encryptionService;
    private final QuizSessionService sessionService;
    private final ImageBlobService imageBlobService;

    private TelegramBotsApi botsApi;
    private final Map<Long, BotSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<Long, QuizBotSession> activeBots = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void onApplicationReady() {
        try {
            botsApi = new TelegramBotsApi(DefaultBotSession.class);
        } catch (TelegramApiException e) {
            log.error("Failed to initialise TelegramBotsApi: {}", e.getMessage());
            return;
        }

        quizRepository.findAllByStatus(QuizStatus.ACTIVE).forEach(quiz -> {
            try {
                String token = encryptionService.decrypt(quiz.getBotToken());
                List<QuizBotQuestion> botQuestions = quiz.getQuizQuestions().stream()
                        .sorted(Comparator.comparingInt(qq -> qq.getOrderIndex()))
                        .map(qq -> {
                            var q = qq.getQuestion();
                            return new QuizBotQuestion(
                                    q.getId(),
                                    q.getQuestionBlocks() != null ? q.getQuestionBlocks() : List.of(),
                                    q.getOptions() != null ? q.getOptions() : List.of(),
                                    q.getAnswer(),
                                    q.isBriefing(),
                                    q.isExpectPhoto(),
                                    q.isExpectsTextInput(),
                                    q.getBriefingPrimaryButtonText(),
                                    q.isShowBriefingPrimaryButton(),
                                    q.getBriefingSecondaryButtonText(),
                                    q.isShowBriefingSecondaryButton(),
                                    q.getAfterAnswerButtonText(),
                                    q.isShowAfterAnswerButton(),
                                    q.getHintBlocks() != null ? q.getHintBlocks() : List.of(),
                                    q.getExplanationBlocks() != null ? q.getExplanationBlocks() : List.of()
                            );
                        })
                        .toList();

                QuizBotData data = new QuizBotData(
                        quiz.getId(),
                        quiz.getName(),
                        token,
                        quiz.getBotUsername(),
                        quiz.getTimePerQuestionSeconds(),
                        quiz.getPassScorePercent(),
                        botQuestions
                );
                startBot(data);
            } catch (Exception e) {
                log.error("Failed to restart bot for quiz {}: {}", quiz.getId(), e.getMessage());
            }
        });
    }

    public void startBot(QuizBotData data) {
        stopBot(data.quizId()); // stop any existing session first
        if (botsApi == null) {
            log.warn("TelegramBotsApi not initialised — bot for quiz {} will not start", data.quizId());
            return;
        }
        try {
            QuizBotSession bot = new QuizBotSession(data, sessionService, imageBlobService);
            BotSession session = botsApi.registerBot(bot);
            activeSessions.put(data.quizId(), session);
            activeBots.put(data.quizId(), bot);
            log.info("Started Telegram bot for quiz {} (@{})", data.quizId(), data.botUsername());
        } catch (TelegramApiException e) {
            log.error("Failed to start Telegram bot for quiz {}: {}", data.quizId(), e.getMessage());
            throw new RuntimeException("Failed to start Telegram bot: " + e.getMessage(), e);
        }
    }

    public void stopBot(Long quizId) {
        BotSession session = activeSessions.remove(quizId);
        QuizBotSession bot = activeBots.remove(quizId);
        if (session != null) {
            session.stop();
            log.info("Stopped Telegram bot for quiz {}", quizId);
        }
        if (bot != null) {
            bot.shutdown();
        }
    }

    @PreDestroy
    public void shutdownAll() {
        activeSessions.keySet().toArray(Long[]::new);
        new java.util.ArrayList<>(activeSessions.keySet()).forEach(this::stopBot);
    }
}
