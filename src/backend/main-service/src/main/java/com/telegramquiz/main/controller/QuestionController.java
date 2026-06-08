package com.telegramquiz.main.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.telegramquiz.main.dto.FolderAssignRequestDto;
import com.telegramquiz.main.dto.QuestionRequestDto;
import com.telegramquiz.main.dto.QuestionResponseDto;
import com.telegramquiz.main.dto.ReorderRequestDto;
import com.telegramquiz.main.service.QuestionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<List<QuestionResponseDto>> list(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok(questionService.findAll(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponseDto> get(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(questionService.findById(id, username));
    }

    @PostMapping
    public ResponseEntity<QuestionResponseDto> create(
            @Valid @RequestBody QuestionRequestDto dto,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.create(dto, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequestDto dto,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(questionService.update(id, dto, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {
        questionService.delete(id, username);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/folder")
    public ResponseEntity<QuestionResponseDto> assignFolder(
            @PathVariable Long id,
            @RequestBody FolderAssignRequestDto dto,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(questionService.assignFolder(id, dto.folderId(), username));
    }

    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorder(
            @Valid @RequestBody ReorderRequestDto dto,
            @AuthenticationPrincipal String username) {
        questionService.reorder(dto.orderedIds(), username);
        return ResponseEntity.noContent().build();
    }
}
