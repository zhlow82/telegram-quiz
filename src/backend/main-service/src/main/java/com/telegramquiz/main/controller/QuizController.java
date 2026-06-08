package com.telegramquiz.main.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.telegramquiz.main.dto.QuizRequestDto;
import com.telegramquiz.main.dto.QuizResponseDto;
import com.telegramquiz.main.dto.QuizSessionDto;
import com.telegramquiz.main.dto.QuizSummaryDto;
import com.telegramquiz.main.service.QuizService;
import com.telegramquiz.main.service.QuizSessionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuizSessionService quizSessionService;

    @GetMapping
    public ResponseEntity<List<QuizSummaryDto>> list(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok(quizService.findAll(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResponseDto> get(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(quizService.findById(id, username));
    }

    @PostMapping
    public ResponseEntity<QuizResponseDto> create(
            @Valid @RequestBody QuizRequestDto dto,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.create(dto, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody QuizRequestDto dto,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(quizService.update(id, dto, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {
        quizService.delete(id, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<QuizResponseDto> activate(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(quizService.activate(id, username));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<QuizResponseDto> stop(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(quizService.stop(id, username));
    }

    @GetMapping("/{id}/sessions")
    public ResponseEntity<List<QuizSessionDto>> getSessions(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {
        // Verify ownership before returning sessions
        quizService.findById(id, username);
        return ResponseEntity.ok(quizSessionService.findByQuizId(id));
    }
}
