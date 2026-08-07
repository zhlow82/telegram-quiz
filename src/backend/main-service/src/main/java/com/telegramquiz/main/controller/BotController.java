package com.telegramquiz.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.telegramquiz.main.dto.ValidateTokenRequestDto;
import com.telegramquiz.main.dto.ValidateTokenResponseDto;
import com.telegramquiz.main.entity.Quiz;
import com.telegramquiz.main.service.QuizService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bot")
@RequiredArgsConstructor
public class BotController {

    private final RestClient restClient = RestClient.create();
    private final QuizService quizService;

    @PostMapping("/validate-token")
    public ResponseEntity<ValidateTokenResponseDto> validateToken(
            @Valid @RequestBody ValidateTokenRequestDto dto) {
        try {
            TelegramGetMeResponse response = restClient.get()
                    .uri("https://api.telegram.org/bot{token}/getMe", dto.token())
                    .retrieve()
                    .body(TelegramGetMeResponse.class);

            if (response != null && response.ok() && response.result() != null) {
                Quiz conflict = quizService.findTokenConflict(dto.token(), dto.excludeQuizId());
                return ResponseEntity.ok(new ValidateTokenResponseDto(
                        true,
                        response.result().firstName(),
                        response.result().username(),
                        conflict != null,
                        conflict != null ? conflict.getName() : null
                ));
            }
            return ResponseEntity.ok(new ValidateTokenResponseDto(false, null, null, false, null));
        } catch (RestClientException e) {
            return ResponseEntity.ok(new ValidateTokenResponseDto(false, null, null, false, null));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TelegramGetMeResponse(boolean ok, TelegramBotInfo result) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TelegramBotInfo(
            long id,
            @JsonProperty("first_name") String firstName,
            String username
    ) {}
}
