package com.example.wieisbob.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest (
        @Email @NotBlank String email,
        @NotBlank String name,
        @NotBlank String password
) {}
