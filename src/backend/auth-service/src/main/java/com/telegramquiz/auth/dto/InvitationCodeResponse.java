package com.telegramquiz.auth.dto;

import java.time.LocalDateTime;

public record InvitationCodeResponse(
        Long id,
        String code,
        String createdBy,
        LocalDateTime createdAt,
        boolean active
) {}
