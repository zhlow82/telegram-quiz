package com.telegramquiz.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.telegramquiz.main.entity.Quiz;
import com.telegramquiz.main.entity.QuizStatus;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findAllByOrderByCreatedAtDesc();
    List<Quiz> findAllByStatus(QuizStatus status);
}
