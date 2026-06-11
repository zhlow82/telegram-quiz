package com.telegramquiz.auth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.telegramquiz.auth.config.InMemoryLogAppender;
import com.telegramquiz.auth.config.InMemoryLogAppender.LogEntry;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/admin/logs")
@RequiredArgsConstructor
public class LogController {

    private final InMemoryLogAppender logAppender;

    @GetMapping
    public ResponseEntity<List<LogEntry>> getLogs(
            @AuthenticationPrincipal String username,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "200") int limit) {
        return ResponseEntity.ok(logAppender.getLogs(level, search, limit));
    }
}
