package com.example.wieisbob.bobassignment.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BobAssignmentRequest(
        @NotNull LocalDateTime assignedAt
) {}
