package com.telegramquiz.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.telegramquiz.main.entity.QuizQuestion;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {}
