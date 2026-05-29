package com.telegramquiz.main.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "questions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(columnDefinition = "TEXT")
    private String intro;

    @Column(name = "intro_blue", nullable = false)
    private boolean introBlue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "question_image_paths", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<String> questionImagePaths = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<String> options = new ArrayList<>();

    private String answer;

    @Column(name = "expect_photo", nullable = false)
    private boolean expectPhoto;

    @Column(name = "is_briefing", nullable = false)
    private boolean isBriefing;

    @Column(name = "hint_text", columnDefinition = "TEXT")
    private String hintText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hint_image_paths", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<String> hintImagePaths = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "explanation_texts", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<String> explanationTexts = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "explanation_image_paths", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<String> explanationImagePaths = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
