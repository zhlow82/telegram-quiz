package com.telegramquiz.main.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telegramquiz.main.bot.QuizBotData;
import com.telegramquiz.main.bot.QuizBotQuestion;
import com.telegramquiz.main.bot.TelegramBotManager;
import com.telegramquiz.main.dto.QuizRequestDto;
import com.telegramquiz.main.dto.QuizResponseDto;
import com.telegramquiz.main.dto.QuizSummaryDto;
import com.telegramquiz.main.entity.Question;
import com.telegramquiz.main.entity.Quiz;
import com.telegramquiz.main.entity.QuizQuestion;
import com.telegramquiz.main.entity.QuizStatus;
import com.telegramquiz.main.repository.QuestionRepository;
import com.telegramquiz.main.repository.QuizQuestionRepository;
import com.telegramquiz.main.repository.QuizRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuestionRepository questionRepository;
    private final BotTokenEncryptionService encryptionService;
    private final QuestionService questionService;
    private final TelegramBotManager telegramBotManager;

    @Transactional(readOnly = true)
    public List<QuizSummaryDto> findAll(String username) {
        return quizRepository.findAllByCreatedByOrderByCreatedAtDesc(username).stream()
                .map(this::toSummaryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuizResponseDto findById(Long id, String username) {
        return toResponseDto(getOrThrow(id, username));
    }

    @Transactional(readOnly = true)
    public QuizBotData debugBotData(Long id, String username) {
        return buildBotData(getOrThrow(id, username));
    }

    @Transactional
    public QuizResponseDto create(QuizRequestDto dto, String username) {
        Quiz quiz = Quiz.builder()
                .createdBy(username)
                .name(dto.name())
                .botToken(encryptionService.encrypt(dto.botToken()))
                .botUsername(dto.botUsername())
                .timePerQuestionSeconds(dto.timePerQuestionSeconds())
                .totalTimeLimitSeconds(dto.totalTimeLimitSeconds())
                .passScorePercent(dto.passScorePercent())
                .status(QuizStatus.DRAFT)
                .build();
        quiz = quizRepository.save(quiz);
        saveQuizQuestions(quiz, dto.questionIds(), username);
        return toResponseDto(quizRepository.findById(quiz.getId()).orElseThrow());
    }

    @Transactional
    public QuizResponseDto update(Long id, QuizRequestDto dto, String username) {
        Quiz quiz = getOrThrow(id, username);
        quiz.setName(dto.name());
        // Only re-encrypt if a new (non-masked) token is provided
        if (!dto.botToken().startsWith("…")) {
            quiz.setBotToken(encryptionService.encrypt(dto.botToken()));
        }
        if (dto.botUsername() != null) {
            quiz.setBotUsername(dto.botUsername());
        }
        quiz.setTimePerQuestionSeconds(dto.timePerQuestionSeconds());
        quiz.setTotalTimeLimitSeconds(dto.totalTimeLimitSeconds());
        quiz.setPassScorePercent(dto.passScorePercent());
        quiz.getQuizQuestions().clear();
        quizRepository.save(quiz);
        saveQuizQuestions(quiz, dto.questionIds(), username);
        return toResponseDto(quizRepository.findById(id).orElseThrow());
    }

    @Transactional
    public QuizResponseDto activate(Long id, String username) {
        Quiz quiz = getOrThrow(id, username);
        quiz.setStatus(QuizStatus.ACTIVE);
        Quiz saved = quizRepository.save(quiz);
        telegramBotManager.startBot(buildBotData(saved));
        return toResponseDto(saved);
    }

    @Transactional
    public QuizResponseDto stop(Long id, String username) {
        Quiz quiz = getOrThrow(id, username);
        quiz.setStatus(QuizStatus.STOPPED);
        Quiz saved = quizRepository.save(quiz);
        telegramBotManager.stopBot(id);
        return toResponseDto(saved);
    }

    @Transactional
    public void delete(Long id, String username) {
        if (!quizRepository.findByIdAndCreatedBy(id, username).isPresent()) {
            throw new EntityNotFoundException("Quiz not found: " + id);
        }
        quizRepository.deleteById(id);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private void saveQuizQuestions(Quiz quiz, List<Long> questionIds, String username) {
        List<QuizQuestion> items = new ArrayList<>();
        for (int i = 0; i < questionIds.size(); i++) {
            Long qId = questionIds.get(i);
            Question question = questionRepository.findById(qId)
                .filter(q -> questionService.canAccess(q, username))
                .orElseThrow(() -> new EntityNotFoundException("Question not found: " + qId));
            items.add(QuizQuestion.builder()
                    .quiz(quiz)
                    .question(question)
                    .orderIndex(i)
                    .build());
        }
        quizQuestionRepository.saveAll(items);
    }

    private Quiz getOrThrow(Long id, String username) {
        return quizRepository.findByIdAndCreatedBy(id, username)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + id));
    }

    private QuizSummaryDto toSummaryDto(Quiz quiz) {
        return new QuizSummaryDto(
                quiz.getId(),
                quiz.getName(),
                quiz.getBotUsername(),
                quiz.getStatus().name(),
                quiz.getQuizQuestions().size(),
                quiz.getTimePerQuestionSeconds(),
                quiz.getTotalTimeLimitSeconds(),
                quiz.getPassScorePercent(),
                quiz.getCreatedAt()
        );
    }

    private QuizResponseDto toResponseDto(Quiz quiz) {
        String masked = maskToken(encryptionService.decrypt(quiz.getBotToken()));
        var questions = quiz.getQuizQuestions().stream()
                .map(qq -> questionService.toDto(qq.getQuestion()))
                .toList();
        return new QuizResponseDto(
                quiz.getId(),
                quiz.getName(),
                masked,
                quiz.getBotUsername(),
                quiz.getTimePerQuestionSeconds(),
                quiz.getTotalTimeLimitSeconds(),
                quiz.getPassScorePercent(),
                quiz.getStatus().name(),
                quiz.getCreatedAt(),
                questions
        );
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 4) return "****";
        return "…" + token.substring(token.length() - 4);
    }

    private QuizBotData buildBotData(Quiz quiz) {
        String decryptedToken = encryptionService.decrypt(quiz.getBotToken());
        java.util.Comparator<QuizQuestion> byOrder = java.util.Comparator.comparingInt(QuizQuestion::getOrderIndex);
        List<QuizBotQuestion> botQuestions = quiz.getQuizQuestions().stream()
                .sorted(byOrder)
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
        return new QuizBotData(
                quiz.getId(),
                quiz.getName(),
                decryptedToken,
                quiz.getBotUsername(),
                quiz.getTimePerQuestionSeconds(),
                quiz.getPassScorePercent(),
                botQuestions
        );
    }}
