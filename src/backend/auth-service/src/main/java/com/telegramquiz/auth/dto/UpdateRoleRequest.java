package com.telegramquiz.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateRoleRequest(
        @NotBlank
        @Pattern(regexp = "ROLE_ADMIN|ROLE_MEMBER", message = "Role must be ROLE_ADMIN or ROLE_MEMBER")
        String role
) {}
