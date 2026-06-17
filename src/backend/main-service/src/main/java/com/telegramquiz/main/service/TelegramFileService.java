package com.telegramquiz.main.service;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegramquiz.main.entity.Quiz;
import com.telegramquiz.main.entity.QuizSession;
import com.telegramquiz.main.repository.QuizRepository;
import com.telegramquiz.main.repository.QuizSessionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramFileService {

    private final QuizSessionRepository sessionRepository;
    private final QuizRepository quizRepository;
    private final BotTokenEncryptionService encryptionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public ResponseEntity<byte[]> downloadPhoto(Long sessionId, String photoFileId) {
        log.info("downloadPhoto called: sessionId={}, photoFileId={}", sessionId, photoFileId);
        QuizSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));

        Quiz quiz = quizRepository.findById(session.getQuizId())
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + session.getQuizId()));

        String botToken = encryptionService.decrypt(quiz.getBotToken());

        try {
            String filePath = getFilePath(botToken, photoFileId);
            if (filePath == null) {
                log.warn("Could not get file path for photo {} in session {}", photoFileId, sessionId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("Cache-Control", "no-store")
                        .body("Photo not found on Telegram".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            byte[] bytes = downloadFile(botToken, filePath);
            String contentType = filePath.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Cache-Control", "public, max-age=86400")
                    .body(bytes);
        } catch (Exception e) {
            log.error("Failed to download photo {} for session {}: {}", photoFileId, sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Cache-Control", "no-store")
                    .body("Photo download failed".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private String getFilePath(String botToken, String fileId) throws Exception {
        String url = "https://api.telegram.org/bot" + botToken + "/getFile?file_id=" + java.net.URLEncoder.encode(fileId, java.nio.charset.StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = objectMapper.readTree(response.body());
        if (!root.path("ok").asBoolean(false)) {
            log.warn("Telegram API error: {}", root.path("description").asText("unknown"));
            return null;
        }

        String filePath = root.path("result").path("file_path").asText(null);
        if (filePath == null || filePath.isBlank()) {
            log.warn("No file_path in Telegram response for file_id: {}", fileId);
            return null;
        }
        return filePath;
    }

    private byte[] downloadFile(String botToken, String filePath) throws Exception {
        String url = "https://api.telegram.org/file/bot" + botToken + "/" + filePath;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<byte[]> response = HTTP.send(request, HttpResponse.BodyHandlers.ofByteArray());
        return response.body();
    }
}
