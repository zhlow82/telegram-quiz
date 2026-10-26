package com.telegramquiz.main.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.telegramquiz.main.bot.TelegramBotManager;
import com.telegramquiz.main.dto.QuizResponseDto;
import com.telegramquiz.main.entity.Quiz;
import com.telegramquiz.main.entity.QuizStatus;
import com.telegramquiz.main.repository.QuestionRepository;
import com.telegramquiz.main.repository.QuizQuestionRepository;
import com.telegramquiz.main.repository.QuizRepository;

@ExtendWith(MockitoExtension.class)
class QuizActivationTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private BotTokenEncryptionService encryptionService;

    @Mock
    private QuestionService questionService;

    @Mock
    private TelegramBotManager telegramBotManager;

    private QuizService service;

    @BeforeEach
    void setUp() {
        service = new QuizService(quizRepository, quizQuestionRepository, questionRepository,
                encryptionService, questionService, telegramBotManager);
    }

    @Test
    @DisplayName("quiz-authoring / One active quiz per bot token / Conflict returns 409 and names the quiz")
    void activationRejectsTokenAlreadyInUse() {
        Quiz target = quiz(1L, "alice", "enc-target", QuizStatus.DRAFT);
        Quiz conflict = quiz(2L, "bob", "enc-conflict", QuizStatus.ACTIVE);

        when(quizRepository.findByIdAndCreatedBy(1L, "alice")).thenReturn(Optional.of(target));
        when(encryptionService.decrypt("enc-target")).thenReturn("123:ABC");
        when(quizRepository.findAllByStatus(QuizStatus.ACTIVE)).thenReturn(List.of(conflict));
        when(encryptionService.decrypt("enc-conflict")).thenReturn("123:ABC");

        assertThatThrownBy(() -> service.activate(1L, "alice"))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode().value()).isEqualTo(409);
                    assertThat(ex.getReason()).contains("already in use").contains("Quiz 2");
                });

        verify(telegramBotManager, never()).startBot(any());
    }

    @Test
    @DisplayName("quiz-authoring / Quiz lifecycle / Activation starts the bot and marks the quiz ACTIVE")
    void activationStartsBot() {
        Quiz target = quiz(1L, "alice", "enc-target", QuizStatus.DRAFT);

        when(quizRepository.findByIdAndCreatedBy(1L, "alice")).thenReturn(Optional.of(target));
        when(encryptionService.decrypt("enc-target")).thenReturn("123:ABC");
        when(quizRepository.findAllByStatus(QuizStatus.ACTIVE)).thenReturn(List.of());
        when(quizRepository.save(target)).thenReturn(target);

        QuizResponseDto dto = service.activate(1L, "alice");

        assertThat(dto.status()).isEqualTo("ACTIVE");
        assertThat(target.getStatus()).isEqualTo(QuizStatus.ACTIVE);
        verify(telegramBotManager).startBot(any());
    }

    @Test
    @DisplayName("quiz-authoring / Quiz lifecycle / Stopping a quiz stops the bot and marks it STOPPED")
    void stoppingStopsBot() {
        Quiz target = quiz(1L, "alice", "enc-target", QuizStatus.ACTIVE);

        when(quizRepository.findByIdAndCreatedBy(1L, "alice")).thenReturn(Optional.of(target));
        when(quizRepository.save(target)).thenReturn(target);

        QuizResponseDto dto = service.stop(1L, "alice");

        assertThat(dto.status()).isEqualTo("STOPPED");
        verify(telegramBotManager).stopBot(1L);
    }

    @Test
    @DisplayName("quiz-authoring / Bot token secrecy / Only the last four characters are returned")
    void tokenIsMasked() {
        Quiz target = quiz(1L, "alice", "enc-target", QuizStatus.DRAFT);

        when(quizRepository.findByIdAndCreatedBy(1L, "alice")).thenReturn(Optional.of(target));
        when(encryptionService.decrypt("enc-target")).thenReturn("123456:ABCDEF");

        QuizResponseDto dto = service.findById(1L, "alice");

        assertThat(dto.botTokenMasked()).isEqualTo("…CDEF");
    }

    private Quiz quiz(Long id, String owner, String encryptedToken, QuizStatus status) {
        return Quiz.builder()
                .id(id)
                .createdBy(owner)
                .name("Quiz " + id)
                .botToken(encryptedToken)
                .botUsername("bot")
                .timePerQuestionSeconds(30)
                .totalTimeLimitSeconds(0)
                .passScorePercent(70)
                .status(status)
                .build();
    }
}
