package com.example.wieisbob.bobassignment.dto;

import java.time.LocalDateTime;

public record BobAssignmentResponse(
   Long id,
   Long userId,
   Long groupId,
   LocalDateTime assignedAt
) {}
