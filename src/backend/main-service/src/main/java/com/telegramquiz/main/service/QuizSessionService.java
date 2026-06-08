package com.telegramquiz.main.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telegramquiz.main.dto.QuizSessionDto;
import com.telegramquiz.main.entity.QuizSession;
import com.telegramquiz.main.entity.QuizSessionStatus;
import com.telegramquiz.main.repository.QuizSessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizSessionService {

    private final QuizSessionRepository repository;

    @Transactional
    public void recordStarted(Long quizId, int totalQuestions,
                               long telegramUserId, String username, String firstName) {
        // If participant restarts mid-quiz, remove their in-progress session
        repository.findByQuizIdAndTelegramUserIdAndStatus(quizId, telegramUserId, QuizSessionStatus.IN_PROGRESS)
                .ifPresent(repository::delete);

        repository.save(QuizSession.builder()
                .quizId(quizId)
                .telegramUserId(telegramUserId)
                .telegramUsername(username)
                .telegramFirstName(firstName)
                .teamName(null)
                .score(0)
                .totalQuestions(totalQuestions)
                .passed(false)
                .status(QuizSessionStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public void recordCompleted(Long quizId, long telegramUserId,
                                 int score, int total, boolean passed) {
        repository.findByQuizIdAndTelegramUserIdAndStatus(quizId, telegramUserId, QuizSessionStatus.IN_PROGRESS)
                .ifPresent(session -> {
                    session.setScore(score);
                    session.setTotalQuestions(total);
                    session.setPassed(passed);
                    session.setStatus(QuizSessionStatus.COMPLETED);
                    session.setCompletedAt(LocalDateTime.now());
                    repository.save(session);
                });
    }

        @Transactional
        public void recordTeamName(Long quizId, long telegramUserId, String teamName) {
                repository.findByQuizIdAndTelegramUserIdAndStatus(quizId, telegramUserId, QuizSessionStatus.IN_PROGRESS)
                                .ifPresent(session -> {
                                        session.setTeamName(teamName);
                                        repository.save(session);
                                });
        }

    @Transactional(readOnly = true)
    public List<QuizSessionDto> findByQuizId(Long quizId) {
        return repository.findAllByQuizIdOrderByStartedAtDesc(quizId).stream()
                .map(s -> new QuizSessionDto(
                        s.getId(),
                        s.getTelegramUserId(),
                        s.getTelegramUsername(),
                        s.getTelegramFirstName(),
                        s.getTeamName(),
                        s.getScore(),
                        s.getTotalQuestions(),
                        s.isPassed(),
                        s.getStatus().name(),
                        s.getStartedAt(),
                        s.getCompletedAt()
                ))
                .toList();
    }
}
