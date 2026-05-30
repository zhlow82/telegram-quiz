package com.telegramquiz.auth.dto;

public record UserResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email,
        String provider,
        String role,
        boolean active
) {}
