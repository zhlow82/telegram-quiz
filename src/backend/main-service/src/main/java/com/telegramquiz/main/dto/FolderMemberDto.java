package com.telegramquiz.main.dto;

import java.time.LocalDateTime;

public record FolderMemberDto(
        Long id,
        Long folderId,
        String folderName,
        String username,
        String role,
        String status,
        String invitedBy,
        LocalDateTime createdAt
) {}
