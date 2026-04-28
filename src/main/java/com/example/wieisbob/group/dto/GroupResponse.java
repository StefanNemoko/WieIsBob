package com.example.wieisbob.group.dto;

import java.time.LocalDateTime;

public record GroupResponse(
    Long id,
    String name,
    LocalDateTime createdAt
) {}
