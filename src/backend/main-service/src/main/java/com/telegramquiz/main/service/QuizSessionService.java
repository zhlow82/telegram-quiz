package com.telegramquiz.main.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telegramquiz.main.dto.QuizSessionAnswerDto;
import com.telegramquiz.main.dto.QuizSessionDto;
import com.telegramquiz.main.entity.QuizSession;
import com.telegramquiz.main.entity.QuizSessionAnswer;
import com.telegramquiz.main.entity.QuizSessionStatus;
import com.telegramquiz.main.repository.QuizSessionAnswerRepository;
import com.telegramquiz.main.repository.QuizSessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizSessionService {

    private final QuizSessionRepository repository;
    private final QuizSessionAnswerRepository answerRepository;

    @Transactional
    public Long recordStarted(Long quizId, int totalQuestions,
                               long telegramUserId, String username, String firstName) {
        repository.findByQuizIdAndTelegramUserIdAndStatus(quizId, telegramUserId, QuizSessionStatus.IN_PROGRESS)
                .ifPresent(session -> {
                    session.setStatus(QuizSessionStatus.ABANDONED);
                    session.setAbandonedAt(LocalDateTime.now());
                    repository.save(session);
                });

        LocalDateTime now = LocalDateTime.now();
        QuizSession session = repository.save(QuizSession.builder()
                .quizId(quizId)
                .telegramUserId(telegramUserId)
                .telegramUsername(username)
                .telegramFirstName(firstName)
                .teamName(null)
                .score(0)
                .totalQuestions(totalQuestions)
                .passed(false)
                .status(QuizSessionStatus.IN_PROGRESS)
                .currentQuestionIndex(0)
                .startedAt(now)
                .lastActivityAt(now)
                .build());
        return session.getId();
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
                    session.setLastActivityAt(LocalDateTime.now());
                    repository.save(session);
                });
    }

    @Transactional
    public void recordTeamName(Long quizId, long telegramUserId, String teamName) {
        repository.findByQuizIdAndTelegramUserIdAndStatus(quizId, telegramUserId, QuizSessionStatus.IN_PROGRESS)
                .ifPresent(session -> {
                    session.setTeamName(teamName);
                    session.setLastActivityAt(LocalDateTime.now());
                    repository.save(session);
                });
    }

    @Transactional
    public void recordAnswer(Long sessionId, Long questionId, String selectedAnswer,
                              Boolean isCorrect, String photoFileId, String photoCaption, Integer responseTimeMs) {
        answerRepository.save(QuizSessionAnswer.builder()
                .sessionId(sessionId)
                .questionId(questionId)
                .selectedAnswer(selectedAnswer)
                .isCorrect(isCorrect)
                .photoFileId(photoFileId)
                .photoCaption(photoCaption)
                .responseTimeMs(responseTimeMs)
                .answeredAt(LocalDateTime.now())
                .build());

        repository.findById(sessionId).ifPresent(session -> {
            session.setCurrentQuestionIndex(session.getCurrentQuestionIndex() + 1);
            session.setLastActivityAt(LocalDateTime.now());
            repository.save(session);
        });
    }

    @Transactional(readOnly = true)
    public List<QuizSessionAnswerDto> getAnswersBySessionId(Long sessionId) {
        return answerRepository.findBySessionIdOrderByAnsweredAtAsc(sessionId).stream()
                .map(a -> new QuizSessionAnswerDto(
                        a.getId(),
                        a.getSessionId(),
                        a.getQuestionId(),
                        a.getSelectedAnswer(),
                        a.getIsCorrect(),
                        a.getPhotoFileId(),
                        a.getPhotoCaption(),
                        a.getResponseTimeMs(),
                        a.getAnsweredAt()
                ))
                .toList();
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
                        s.getCurrentQuestionIndex(),
                        s.getStartedAt(),
                        s.getCompletedAt(),
                        s.getLastActivityAt(),
                        s.getAbandonedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Long> findQuizIdBySessionId(Long sessionId) {
        return repository.findById(sessionId).map(QuizSession::getQuizId);
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupAbandonedSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        List<QuizSession> stale = repository.findByStatusAndLastActivityAtBefore(
                QuizSessionStatus.IN_PROGRESS, cutoff);
        for (QuizSession session : stale) {
            session.setStatus(QuizSessionStatus.ABANDONED);
            session.setAbandonedAt(LocalDateTime.now());
        }
        if (!stale.isEmpty()) {
            repository.saveAll(stale);
            log.info("Cleaned up {} abandoned sessions", stale.size());
        }
    }
}
