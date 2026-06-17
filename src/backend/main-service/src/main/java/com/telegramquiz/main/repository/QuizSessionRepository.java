package com.telegramquiz.main.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.telegramquiz.main.entity.QuizSession;
import com.telegramquiz.main.entity.QuizSessionStatus;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    List<QuizSession> findAllByQuizIdOrderByStartedAtDesc(Long quizId);
    Optional<QuizSession> findByQuizIdAndTelegramUserIdAndStatus(Long quizId, Long telegramUserId, QuizSessionStatus status);
    List<QuizSession> findByStatusAndLastActivityAtBefore(QuizSessionStatus status, LocalDateTime cutoff);
}
