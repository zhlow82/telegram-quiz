package com.telegramquiz.main.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "quiz_session_answers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSessionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "selected_answer", length = 500)
    private String selectedAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "photo_file_id", length = 500)
    private String photoFileId;

    @Column(name = "photo_caption", columnDefinition = "TEXT")
    private String photoCaption;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;
}
