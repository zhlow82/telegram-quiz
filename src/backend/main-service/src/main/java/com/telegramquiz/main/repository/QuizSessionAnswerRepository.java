package com.telegramquiz.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.telegramquiz.main.entity.QuizSessionAnswer;

public interface QuizSessionAnswerRepository extends JpaRepository<QuizSessionAnswer, Long> {
    List<QuizSessionAnswer> findBySessionIdOrderByAnsweredAtAsc(Long sessionId);
    Optional<QuizSessionAnswer> findFirstBySessionIdAndQuestionIdOrderByAnsweredAtDesc(Long sessionId, Long questionId);
    void deleteBySessionId(Long sessionId);
}
