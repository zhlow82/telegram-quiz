package com.telegramquiz.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        String firstName,
        String lastName
) {}
