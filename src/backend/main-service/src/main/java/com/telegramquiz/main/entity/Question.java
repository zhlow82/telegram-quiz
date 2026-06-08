package com.telegramquiz.main.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.telegramquiz.main.model.ContentBlock;

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

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "question_blocks", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<ContentBlock> questionBlocks = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<String> options = new ArrayList<>();

    private String answer;

    @Column(name = "expect_photo", nullable = false)
    private boolean expectPhoto;

    @Column(name = "is_briefing", nullable = false)
    private boolean isBriefing;

    @Column(name = "expects_text_input", nullable = false)
    private boolean expectsTextInput;

    @Column(name = "briefing_primary_button_text")
    private String briefingPrimaryButtonText;

    @Column(name = "show_briefing_primary_button", nullable = false)
    private boolean showBriefingPrimaryButton;

    @Column(name = "briefing_secondary_button_text")
    private String briefingSecondaryButtonText;

    @Column(name = "show_briefing_secondary_button", nullable = false)
    private boolean showBriefingSecondaryButton;

    @Column(name = "after_answer_button_text")
    private String afterAnswerButtonText;

    @Column(name = "show_after_answer_button", nullable = false)
    private boolean showAfterAnswerButton;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hint_blocks", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<ContentBlock> hintBlocks = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "explanation_blocks", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<ContentBlock> explanationBlocks = new ArrayList<>();

    private Integer mark;

    @Column(name = "folder_id")
    private Long folderId;

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
