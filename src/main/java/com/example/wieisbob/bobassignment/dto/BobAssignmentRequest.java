package com.example.wieisbob.bobassignment.dto;

import java.time.LocalDateTime;

public record BobAssignmentRequest(
        LocalDateTime assignedAt
) {}
