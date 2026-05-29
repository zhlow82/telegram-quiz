package com.telegramquiz.main.repository;

import com.telegramquiz.main.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByOrderByOrderIndexAsc();
}
