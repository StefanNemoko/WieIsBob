package com.example.wieisbob.auth.dto;

import java.time.LocalDateTime;

public record AuthResponse (
        String bearer,
        LocalDateTime expiresAt
) {}
