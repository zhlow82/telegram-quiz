package com.telegramquiz.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.telegramquiz.main.entity.QuizSessionAnswer;

public interface QuizSessionAnswerRepository extends JpaRepository<QuizSessionAnswer, Long> {
    List<QuizSessionAnswer> findBySessionIdOrderByAnsweredAtAsc(Long sessionId);
    void deleteBySessionId(Long sessionId);
}
